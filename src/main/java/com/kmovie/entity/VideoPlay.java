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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    @Column(name = "title_id", nullable = false)
    private UUID titleId;

    @Column(name = "start_play_time", nullable = false)
    private LocalDateTime startPlayTime;

    @Column(name = "end_play_time")
    private LocalDateTime endPlayTime;

    @Column(name = "playback_start_point")
    private Long playbackStartPoint;

    @Column(name = "playback_end_point")
    private Long playbackEndPoint;
}
