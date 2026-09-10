package com.kmovie.service;

import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.kmovie.entity.FileEntity;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

/**
 * Google Cloud Storage equivalent of S3StorageService - same method shapes
 * (upload/getObject/delete) so either can be wired in behind a common
 * storage abstraction later, or selected per-environment via config.
 * Not yet wired into the video/profile-picture flows;
 */
@Service
@RequiredArgsConstructor
public class GcsStorageService {

    private final Storage storage;
    private final FileRepository fileRepository;

    @Value("${app.gcs.bucket}")
    private String bucket;

    /** Uploads the given multipart file to GCS and persists a FileEntity row for it. */
    public FileEntity upload(MultipartFile multipartFile, String folder) {
        String key = buildKey(folder, multipartFile.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, key))
                .setContentType(multipartFile.getContentType())
                .build();

        try (InputStream in = multipartFile.getInputStream();
             WriteChannel writer = storage.writer(blobInfo)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            ReadableByteChannel inChannel = Channels.newChannel(in);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                writer.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }

        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(multipartFile.getOriginalFilename());
        fileEntity.setSize(multipartFile.getSize());
        fileEntity.setStorageKey(key);
        fileEntity.setContentType(multipartFile.getContentType());
        return fileRepository.save(fileEntity);
    }

    public boolean exists(String storageKey) {
        Blob blob = storage.get(BlobId.of(bucket, storageKey));
        return blob != null && blob.exists();
    }

    /** Fetches a byte range [start, end] (inclusive) of the object. If both are null, fetches the whole object. */
    public InputStream getObject(String storageKey, Long start, Long end) {
        BlobId blobId = BlobId.of(bucket, storageKey);
        if (storage.get(blobId) == null) {
            throw ApiException.notFound("File not found in Google Cloud Storage: " + storageKey);
        }

        ReadChannel reader = storage.reader(blobId);
        if (start != null) {
            try {
                reader.seek(start);
            } catch (IOException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to seek object: " + e.getMessage());
            }
        }
        if (end != null) {
            // limit() is an exclusive upper bound on the byte offset read from the object.
            reader.limit(end + 1);
        }
        return Channels.newInputStream(reader);
    }

    public void delete(String storageKey) {
        storage.delete(BlobId.of(bucket, storageKey));
    }

    private String buildKey(String folder, String originalFilename) {
        String safeName = originalFilename == null ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return folder + "/" + UUID.randomUUID() + "-" + safeName;
    }
}
