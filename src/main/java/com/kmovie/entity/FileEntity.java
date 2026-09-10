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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "content_type")
    private String contentType;
}
