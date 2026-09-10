package com.kmovie.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Common columns shared by every persisted model:
 * id, dateCreated, dateUpdated, isDeleted, dateDeleted.
 * Deletes are soft-deletes: isDeleted is flagged and dateDeleted stamped,
 * rows are never physically removed.
 */
@MappedSuperclass
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    private LocalDateTime dateUpdated;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime dateDeleted;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dateCreated = now;
        this.dateUpdated = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateUpdated = LocalDateTime.now();
    }

    public void markDeleted() {
        this.isDeleted = true;
        this.dateDeleted = LocalDateTime.now();
    }
}
