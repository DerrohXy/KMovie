package com.kmovie.repository;

import com.kmovie.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByIdAndIsDeletedFalse(UUID id);
}
