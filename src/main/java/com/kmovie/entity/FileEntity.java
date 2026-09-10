package com.kmovie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a file that has been uploaded to AWS S3. `storageKey` is the
 * S3 object key returned at upload time and is the only thing needed to
 * fetch/stream the object back later.
 */
@Entity
@Table(name = "files")
@Getter
@Setter
public class FileEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String storageKey;

    @Column
    private String contentType;
}
