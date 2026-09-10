package com.kmovie.controller;

import com.kmovie.dto.request.VideoUpdateRequest;
import com.kmovie.dto.response.ApiResponse;
import com.kmovie.dto.response.VideoResponse;
import com.kmovie.entity.Video;
import com.kmovie.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movies/videos/{titleId}")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoResponse>>> list(@PathVariable UUID titleId) {
        List<VideoResponse> videos = videoService.listByTitle(titleId).stream()
                .map(VideoResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(videos));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<VideoResponse>> upload(
            @PathVariable UUID titleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long duration) {

        Video video = videoService.upload(titleId, file, name, description, duration);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Video uploaded", new VideoResponse(video)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/{videoId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<VideoResponse>> update(
            @PathVariable UUID titleId, @PathVariable UUID videoId, @RequestBody VideoUpdateRequest request) {
        Video video = videoService.update(videoId, titleId, request);
        return ResponseEntity.ok(ApiResponse.ok("Video updated", new VideoResponse(video)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{videoId}")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@PathVariable UUID titleId, @PathVariable UUID videoId) {
        videoService.deleteOne(videoId, titleId);
        return ResponseEntity.ok(ApiResponse.ok("Video deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAll(@PathVariable UUID titleId) {
        videoService.deleteAllForTitle(titleId);
        return ResponseEntity.ok(ApiResponse.ok("All videos under this title deleted"));
    }
}
