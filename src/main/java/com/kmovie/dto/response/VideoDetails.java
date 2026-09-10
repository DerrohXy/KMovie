package com.kmovie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Technical metadata read off an uploaded video file via ffprobe. */
@Getter
@AllArgsConstructor
public class VideoDetails {
    private final Long durationSeconds;
    private final Integer width;
    private final Integer height;
    /** e.g. "16:9" */
    private final String aspectRatio;
    private final String videoCodec;
    private final String audioCodec;
    private final Double frameRate;
    /** Overall bitrate in bits/second, if reported. */
    private final Long bitrate;
}
