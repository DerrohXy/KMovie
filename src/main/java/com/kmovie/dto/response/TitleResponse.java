package com.kmovie.dto.response;

import com.kmovie.entity.Title;
import com.kmovie.enums.Category;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class TitleResponse {
    private final UUID id;
    private final String name;
    private final UUID parentTitleId;
    private final List<String> genre;
    private final Category category;
    private final LocalDateTime dateCreated;
    private final LocalDateTime dateUpdated;

    public TitleResponse(Title title) {
        this.id = title.getId();
        this.name = title.getName();
        this.parentTitleId = title.getParentTitleId();
        this.genre = title.getGenre() == null || title.getGenre().isBlank()
                ? List.of()
                : java.util.Arrays.stream(title.getGenre().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        this.category = title.getCategory();
        this.dateCreated = title.getDateCreated();
        this.dateUpdated = title.getDateUpdated();
    }
}
