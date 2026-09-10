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

    @Column(nullable = false)
    private UUID titleId;

    @Column(nullable = false)
    private UUID fileId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    /** Duration in seconds. */
    private Long duration;
}
