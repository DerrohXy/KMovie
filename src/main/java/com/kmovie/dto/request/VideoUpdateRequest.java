package com.kmovie.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoUpdateRequest {
    private String name;
    private String description;
}
