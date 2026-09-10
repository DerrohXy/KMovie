package com.kmovie.repository;

import com.kmovie.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    Optional<Video> findByIdAndIsDeletedFalse(UUID id);

    List<Video> findByTitleIdAndIsDeletedFalse(UUID titleId);

    Optional<Video> findByIdAndTitleIdAndIsDeletedFalse(UUID id, UUID titleId);

    long countByTitleIdAndIsDeletedFalse(UUID titleId);
}
