package com.kmovie.dto.request;

import com.kmovie.enums.Category;
import com.kmovie.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TitleRequest {

    @NotBlank(message = "name is required")
    private String name;

    private UUID parentTitleId;

    private List<Genre> genre;

    private Category category;
}
