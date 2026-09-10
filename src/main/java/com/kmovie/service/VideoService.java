package com.kmovie.service;

import com.kmovie.dto.request.VideoUpdateRequest;
import com.kmovie.dto.response.VideoDetails;
import com.kmovie.entity.FileEntity;
import com.kmovie.entity.Title;
import com.kmovie.entity.Video;
import com.kmovie.enums.Category;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import com.kmovie.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final FileRepository fileRepository;
    private final TitleService titleService;
    private final S3StorageService s3StorageService;
    private final VideoDetailsService videoDetailsService;

    public List<Video> listByTitle(UUID titleId) {
        titleService.getById(titleId); // 404s if the title doesn't exist
        return videoRepository.findByTitleIdAndIsDeletedFalse(titleId);
    }

    public Video getByIdAndTitle(UUID videoId, UUID titleId) {
        return videoRepository.findByIdAndTitleIdAndIsDeletedFalse(videoId, titleId)
                .orElseThrow(() -> ApiException.notFound("Video not found under this title"));
    }

    @Transactional
    public Video upload(UUID titleId, MultipartFile file, String name, String description, Long duration) {
        Title title = titleService.getById(titleId);

        // A movie title may only ever hold a single video; series episodes are
        // uploaded under their season (child title) instead.
        if (title.getCategory() == Category.MOVIE
                && videoRepository.countByTitleIdAndIsDeletedFalse(titleId) > 0) {
            throw ApiException.badRequest("This movie title already has a video. Delete it before uploading a new one.");
        }

        // Read duration/resolution/codecs/etc off the file before it's shipped to S3.
        VideoDetails details = videoDetailsService.extract(file);

        FileEntity uploaded = s3StorageService.upload(file, "videos/" + titleId);

        Video video = new Video();
        video.setTitleId(titleId);
        video.setFileId(uploaded.getId());
        video.setName(name != null && !name.isBlank() ? name : file.getOriginalFilename());
        video.setDescription(description);
        // Prefer an explicitly supplied duration, fall back to what ffprobe read from the file.
        video.setDuration(duration != null ? duration : details.getDurationSeconds());
        video.setWidth(details.getWidth());
        video.setHeight(details.getHeight());
        video.setAspectRatio(details.getAspectRatio());
        video.setVideoCodec(details.getVideoCodec());
        video.setAudioCodec(details.getAudioCodec());
        video.setFrameRate(details.getFrameRate());
        video.setBitrate(details.getBitrate());
        return videoRepository.save(video);
    }

    @Transactional
    public Video update(UUID videoId, UUID titleId, VideoUpdateRequest request) {
        Video video = getByIdAndTitle(videoId, titleId);
        if (request.getName() != null && !request.getName().isBlank()) {
            video.setName(request.getName());
        }
        if (request.getDescription() != null) {
            video.setDescription(request.getDescription());
        }
        return videoRepository.save(video);
    }

    @Transactional
    public void deleteOne(UUID videoId, UUID titleId) {
        Video video = getByIdAndTitle(videoId, titleId);
        deleteVideoAndFile(video);
    }

    @Transactional
    public void deleteAllForTitle(UUID titleId) {
        titleService.getById(titleId);
        List<Video> videos = videoRepository.findByTitleIdAndIsDeletedFalse(titleId);
        videos.forEach(this::deleteVideoAndFile);
    }

    private void deleteVideoAndFile(Video video) {
        fileRepository.findByIdAndIsDeletedFalse(video.getFileId()).ifPresent(f -> {
            s3StorageService.delete(f.getStorageKey());
            f.markDeleted();
            fileRepository.save(f);
        });
        video.markDeleted();
        videoRepository.save(video);
    }
}
