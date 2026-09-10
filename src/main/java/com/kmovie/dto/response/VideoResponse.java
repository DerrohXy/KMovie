package com.kmovie.dto.response;

import com.kmovie.entity.Video;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VideoResponse {
    private final UUID id;
    private final UUID titleId;
    private final UUID fileId;
    private final String name;
    private final String description;
    private final Long duration;
    private final Integer width;
    private final Integer height;
    private final String aspectRatio;
    private final String videoCodec;
    private final String audioCodec;
    private final Double frameRate;
    private final Long bitrate;
    private final LocalDateTime dateCreated;

    public VideoResponse(Video video) {
        this.id = video.getId();
        this.titleId = video.getTitleId();
        this.fileId = video.getFileId();
        this.name = video.getName();
        this.description = video.getDescription();
        this.duration = video.getDuration();
        this.width = video.getWidth();
        this.height = video.getHeight();
        this.aspectRatio = video.getAspectRatio();
        this.videoCodec = video.getVideoCodec();
        this.audioCodec = video.getAudioCodec();
        this.frameRate = video.getFrameRate();
        this.bitrate = video.getBitrate();
        this.dateCreated = video.getDateCreated();
    }
}
