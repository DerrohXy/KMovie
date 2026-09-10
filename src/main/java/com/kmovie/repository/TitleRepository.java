package com.kmovie.repository;

import com.kmovie.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TitleRepository extends JpaRepository<Title, UUID>, JpaSpecificationExecutor<Title> {

    Optional<Title> findByIdAndIsDeletedFalse(UUID id);

    List<Title> findByParentTitleIdAndIsDeletedFalse(UUID parentTitleId);
}
