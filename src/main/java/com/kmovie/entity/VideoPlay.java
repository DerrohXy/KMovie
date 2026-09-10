package com.kmovie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A playback record. NOTE: videoId/titleId were added beyond the original
 * spec's {userId, startPlayTime, endPlayTime} fields because a play record
 * that doesn't say *what* was played isn't useful; they're populated
 * automatically by the /movies/play endpoint.
 */
@Entity
@Table(name = "video_plays")
@Getter
@Setter
public class VideoPlay extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID videoId;

    @Column(nullable = false)
    private UUID titleId;

    @Column(nullable = false)
    private LocalDateTime startPlayTime;

    private LocalDateTime endPlayTime;
}
