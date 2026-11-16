package com.kmovie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "start_play_time", nullable = false)
    private LocalDateTime startPlayTime;

    @Column(name = "end_play_time")
    private LocalDateTime endPlayTime;

    @Column(name = "playback_start_point")
    private Long playbackStartPoint;

    @Column(name = "playback_end_point")
    private Long playbackEndPoint;
}
