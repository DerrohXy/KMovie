package com.kmovie.service;

import com.kmovie.entity.FileEntity;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${app.aws.s3.bucket}")
    private String bucket;

    /** Uploads the given multipart file to S3 and persists a FileEntity row for it. */
    public FileEntity upload(MultipartFile multipartFile, String folder) {
        String key = buildKey(folder, multipartFile.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
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

    public HeadObjectResponse head(String storageKey) {
        return s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
    }

    /** Fetches a byte range [start, end] (inclusive) of the object. If both are null, fetches the whole object. */
    public ResponseInputStream<GetObjectResponse> getObject(String storageKey, Long start, Long end) {
        GetObjectRequest.Builder builder = GetObjectRequest.builder().bucket(bucket).key(storageKey);
        if (start != null) {
            String range = "bytes=" + start + "-" + (end != null ? end : "");
            builder.range(range);
        }
        return s3Client.getObject(builder.build());
    }

    public void delete(String storageKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
    }

    private String buildKey(String folder, String originalFilename) {
        String safeName = originalFilename == null ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return folder + "/" + UUID.randomUUID() + "-" + safeName;
    }
}
