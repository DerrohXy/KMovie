package com.kmovie.service;

import com.kmovie.entity.Video;
import com.kmovie.entity.VideoPlay;
import com.kmovie.repository.VideoPlayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaybackService {

    private final VideoPlayRepository videoPlayRepository;

    /** Records that a user started streaming a video (one row per play session). */
    @Transactional
    public void recordPlay(UUID userId, Video video) {
        VideoPlay play = new VideoPlay();
        play.setUserId(userId);
        play.setVideoId(video.getId());
        play.setTitleId(video.getTitleId());
        play.setStartPlayTime(LocalDateTime.now());
        videoPlayRepository.save(play);
    }
}
