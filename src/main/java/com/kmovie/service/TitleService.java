package com.kmovie.service;

import com.kmovie.dto.request.TitleRequest;
import com.kmovie.dto.response.PageResponse;
import com.kmovie.entity.Title;
import com.kmovie.entity.Video;
import com.kmovie.enums.Category;
import com.kmovie.enums.Genre;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.TitleRepository;
import com.kmovie.repository.VideoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;
    private final VideoRepository videoRepository;
    private final S3StorageService s3StorageService;
    private final com.kmovie.repository.FileRepository fileRepository;

    public PageResponse<Title> search(String query, Integer page, Integer count,
                                       UUID parentTitleId, Genre genre, Category category) {
        int pageNum = page == null || page < 0 ? 0 : page;
        int pageSize = count == null || count <= 0 ? 20 : Math.min(count, 100);

        Specification<Title> spec = Specification.where(notDeleted());
        if (query != null && !query.isBlank()) {
            spec = spec.and(nameContains(query));
        }
        if (parentTitleId != null) {
            spec = spec.and(hasParent(parentTitleId));
        }
        if (genre != null) {
            spec = spec.and(hasGenre(genre));
        }
        if (category != null) {
            spec = spec.and(hasCategory(category));
        }

        var pageResult = titleRepository.findAll(spec,
                PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "dateCreated")));

        return new PageResponse<>(pageResult.getContent(), pageNum, pageSize, pageResult.getTotalElements());
    }

    public Title getById(UUID id) {
        return titleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("Title not found"));
    }

    @Transactional
    public Title create(TitleRequest request) {
        if (request.getCategory() == null) {
            throw ApiException.badRequest("category is required");
        }
        if (request.getParentTitleId() != null) {
            // Ensures the parent exists; child titles are used to represent seasons of a series.
            getById(request.getParentTitleId());
        }

        Title title = new Title();
        title.setName(request.getName());
        title.setParentTitleId(request.getParentTitleId());
        title.setCategory(request.getCategory());
        title.setGenre(joinGenres(request.getGenre()));
        return titleRepository.save(title);
    }

    @Transactional
    public Title update(UUID id, TitleRequest request) {
        Title title = getById(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            title.setName(request.getName());
        }
        if (request.getCategory() != null) {
            title.setCategory(request.getCategory());
        }
        if (request.getGenre() != null) {
            title.setGenre(joinGenres(request.getGenre()));
        }
        if (request.getParentTitleId() != null) {
            if (request.getParentTitleId().equals(id)) {
                throw ApiException.badRequest("A title cannot be its own parent");
            }
            getById(request.getParentTitleId());
            title.setParentTitleId(request.getParentTitleId());
        }
        return titleRepository.save(title);
    }

    /** Deletes the title, all of its descendant titles (e.g. seasons), and every video under each. */
    @Transactional
    public void delete(UUID id) {
        Title title = getById(id);
        deleteRecursively(title);
    }

    private void deleteRecursively(Title title) {
        List<Title> children = titleRepository.findByParentTitleIdAndIsDeletedFalse(title.getId());
        for (Title child : children) {
            deleteRecursively(child);
        }

        List<Video> videos = videoRepository.findByTitleIdAndIsDeletedFalse(title.getId());
        for (Video video : videos) {
            fileRepository.findByIdAndIsDeletedFalse(video.getFileId()).ifPresent(f -> {
                s3StorageService.delete(f.getStorageKey());
                f.markDeleted();
                fileRepository.save(f);
            });
            video.markDeleted();
            videoRepository.save(video);
        }

        title.markDeleted();
        titleRepository.save(title);
    }

    private String joinGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }
        return genres.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private Specification<Title> notDeleted() {
        return (root, cq, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    private Specification<Title> nameContains(String query) {
        return (root, cq, cb) -> cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
    }

    private Specification<Title> hasParent(UUID parentTitleId) {
        return (root, cq, cb) -> cb.equal(root.get("parentTitleId"), parentTitleId);
    }

    private Specification<Title> hasCategory(Category category) {
        return (root, cq, cb) -> cb.equal(root.get("category"), category);
    }

    private Specification<Title> hasGenre(Genre genre) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("genre"), genre.name()));
            predicates.add(cb.like(root.get("genre"), genre.name() + ",%"));
            predicates.add(cb.like(root.get("genre"), "%," + genre.name()));
            predicates.add(cb.like(root.get("genre"), "%," + genre.name() + ",%"));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
