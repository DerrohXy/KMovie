package com.kmovie.controller;

import com.kmovie.entity.FileEntity;
import com.kmovie.entity.Video;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import com.kmovie.security.CurrentUserProvider;
import com.kmovie.service.PlaybackService;
import com.kmovie.service.S3StorageService;
import com.kmovie.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Streams video content with HTTP Range support so browsers/players can seek
 * and buffer efficiently instead of downloading the whole file up front.
 */
@RestController
@RequestMapping("/movies/play")
@RequiredArgsConstructor
public class PlayController {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB read/write chunks

    private final VideoService videoService;
    private final FileRepository fileRepository;
    private final S3StorageService s3StorageService;
    private final PlaybackService playbackService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/{titleId}/{videoId}")
    public void play(@PathVariable UUID titleId, @PathVariable UUID videoId,
                      HttpServletRequest request, HttpServletResponse response) throws IOException {

        Video video = videoService.getByIdAndTitle(videoId, titleId);
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(video.getFileId())
                .orElseThrow(() -> ApiException.notFound("Video file not found"));

        long fileSize = file.getSize() != null ? file.getSize() : 0L;
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);

        long start = 0;
        long end = fileSize - 1;
        boolean isPartial = false;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            isPartial = true;
            String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
            try {
                start = ranges[0].isBlank() ? 0 : Long.parseLong(ranges[0]);
                end = (ranges.length > 1 && !ranges[1].isBlank()) ? Long.parseLong(ranges[1]) : fileSize - 1;
            } catch (NumberFormatException e) {
                start = 0;
                end = fileSize - 1;
            }
            if (end > fileSize - 1) {
                end = fileSize - 1;
            }
            if (start > end || start < 0) {
                response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize);
                return;
            }
        }

        long contentLength = end - start + 1;
        MediaType mediaType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.valueOf("video/mp4");

        response.setContentType(mediaType.toString());
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setContentLengthLong(contentLength);

        if (isPartial) {
            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
        } else {
            response.setStatus(HttpStatus.OK.value());
            // Only log a play session the first time a client requests the full stream
            // (i.e. not for every small ranged chunk a seek generates).
            if (currentUserProvider.isAuthenticated()) {
                playbackService.recordPlay(currentUserProvider.getUser().getId(), video);
            }
        }

        try (ResponseInputStream<GetObjectResponse> s3Stream = s3StorageService.getObject(file.getStorageKey(), start, end);
             OutputStream out = response.getOutputStream()) {
            copyStream(s3Stream, out, contentLength);
            out.flush();
        } catch (NoSuchKeyException e) {
            throw ApiException.notFound("Video file is missing from storage");
        }
    }

    private void copyStream(InputStream in, OutputStream out, long limit) throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];
        long remaining = limit;
        int read;
        while (remaining > 0 && (read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
            out.write(buffer, 0, read);
            remaining -= read;
        }
    }
}
