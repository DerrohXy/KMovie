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
    private final LocalDateTime dateCreated;

    public VideoResponse(Video video) {
        this.id = video.getId();
        this.titleId = video.getTitleId();
        this.fileId = video.getFileId();
        this.name = video.getName();
        this.description = video.getDescription();
        this.duration = video.getDuration();
        this.dateCreated = video.getDateCreated();
    }
}
