package com.kmovie.entity;

import com.kmovie.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A movie or series title. Series use child titles to represent seasons
 * (parentTitleId points at the series' own Title row), and episodes are
 * uploaded as Videos under the season title. Movies have no children and
 * take exactly one Video.
 */
@Entity
@Table(name = "titles")
@Getter
@Setter
public class Title extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    /** Self-referencing foreign key; null for a top level title. */
    @Column(name = "parent_title_id")
    private UUID parentTitleId;

    /** Comma-concatenated Genre enum names, e.g. "ACTION,DRAMA". */
    @Column(name = "genre", length = 1000)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;
}
