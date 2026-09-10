package com.kmovie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
public class Video extends BaseEntity {

    @Column(name = "title_id", nullable = false)
    private UUID titleId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    /** Duration in seconds. */
    @Column(name = "duration", nullable = false)
    private Long duration;

    // ---- Technical metadata, extracted from the file at upload time by VideoDetailsService ----
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;
    /** e.g. "16:9" */
    @Column(name = "aspect_ratio")
    private String aspectRatio;

    @Column(name = "video_codec")
    private String videoCodec;

    @Column(name = "audio_codec")
    private String audioCodec;

    @Column(name = "frame_rate")
    private Double frameRate;

    /** Overall bitrate in bits/second, if reported. */
    @Column(name = "bitrate")
    private Long bitrate;
}
