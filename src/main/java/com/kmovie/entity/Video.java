package com.kmovie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "videos")
@Getter
@Setter
public class Video extends BaseEntity {

    @Column(name = "title_id", nullable = false)
    private UUID titleId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    /** Duration in seconds. */
    @Column(name = "duration", nullable = false)
    private Long duration;
}
