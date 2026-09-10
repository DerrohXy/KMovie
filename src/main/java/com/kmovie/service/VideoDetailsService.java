package com.kmovie.service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.github.kokorin.jaffree.ffprobe.Stream;
import com.kmovie.dto.response.VideoDetails;
import com.kmovie.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads technical metadata (duration, resolution, aspect ratio, codecs, frame
 * rate, bitrate) off an uploaded video using ffprobe, via the Jaffree wrapper.
 * Requires the ffmpeg/ffprobe binaries to be present on PATH (or at
 * app.ffmpeg.bin-path) - see the Dockerfile change installing the `ffmpeg`
 * apt package, which provides ffprobe too.
 */
@Slf4j
@Service
public class VideoDetailsService {

    /** Optional explicit directory containing the ffprobe binary; blank = look up PATH. */
    private final Path ffmpegBinPath;

    public VideoDetailsService(@Value("${app.ffmpeg.bin-path:}") String ffmpegBinPath) {
        this.ffmpegBinPath = (ffmpegBinPath == null || ffmpegBinPath.isBlank())
                ? null
                : Path.of(ffmpegBinPath);
    }

    /** Convenience overload: spools the multipart upload to a temp file, probes it, then cleans up. */
    public VideoDetails extract(MultipartFile multipartFile) {
        Path tempFile = null;
        try {
            String suffix = originalSuffix(multipartFile.getOriginalFilename());
            tempFile = Files.createTempFile("kmovie-probe-", suffix);
            multipartFile.transferTo(tempFile);
            return extract(tempFile);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read uploaded video file: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp probe file {}", tempFile, e);
                }
            }
        }
    }

    public VideoDetails extract(Path videoFile) {
        FFprobeResult result;
        try {
            FFprobe ffprobe = ffmpegBinPath != null ? FFprobe.atPath(ffmpegBinPath) : FFprobe.atPath();
            result = ffprobe
                    .setInput(videoFile)
                    .setShowStreams(true)
                    .setShowFormat(true)
                    .execute();
        } catch (RuntimeException e) {
            // Jaffree wraps ffprobe process failures (bad/corrupt file, missing binary, etc.) in a RuntimeException
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read video details: " + e.getMessage());
        }

        Format format = result.getFormat();
        Optional<Stream> videoStream = result.getStreams().stream()
                .filter(s -> s.getCodecType() == StreamType.VIDEO)
                .findFirst();
        Optional<Stream> audioStream = result.getStreams().stream()
                .filter(s -> s.getCodecType() == StreamType.AUDIO)
                .findFirst();

        Long durationSeconds = (long) (format != null && format.getDuration() != null
                        ? Math.round(format.getDuration())
                        : videoStream.map(Stream::getDuration).map(Math::round).orElse(0));

        Integer width = videoStream.map(Stream::getWidth).orElse(0);
        Integer height = videoStream.map(Stream::getHeight).orElse(0);
        String aspectRatio = String.format("%d:%d",width,height);

        float frameRate = 0.0f;
        for (Stream stream : result.getStreams()) {
            if (StreamType.VIDEO == stream.getCodecType()) {
                frameRate = stream.getAvgFrameRate().floatValue();
                break;
            }
        }

        Long bitrate = format != null ? format.getBitRate() : null;

        return new VideoDetails(
                durationSeconds,
                width,
                height,
                aspectRatio,
                videoStream.map(Stream::getCodecName).orElse(null),
                audioStream.map(Stream::getCodecName).orElse(null),
                (double) frameRate,
                bitrate
        );
    }

    private static String computeAspectRatio(Integer width, Integer height) {
        if (width == null || height == null || width <= 0 || height <= 0) {
            return null;
        }
        int divisor = gcd(width, height);
        return (width / divisor) + ":" + (height / divisor);
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /** Jaffree exposes r_frame_rate/avg_frame_rate as a "num/den" fraction string, e.g. "30000/1001". */
    private static Double parseFrameRate(String fraction) {
        if (fraction == null || !fraction.contains("/")) {
            return null;
        }
        String[] parts = fraction.split("/");
        try {
            double num = Double.parseDouble(parts[0]);
            double den = Double.parseDouble(parts[1]);
            return den == 0 ? null : Math.round((num / den) * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String originalSuffix(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".mp4";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
