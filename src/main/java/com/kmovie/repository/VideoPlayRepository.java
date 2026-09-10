package com.kmovie.repository;

import com.kmovie.entity.VideoPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoPlayRepository extends JpaRepository<VideoPlay, UUID> {
}
