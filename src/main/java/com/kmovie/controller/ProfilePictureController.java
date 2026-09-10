package com.kmovie.controller;

import com.kmovie.dto.response.ApiResponse;
import com.kmovie.entity.FileEntity;
import com.kmovie.entity.User;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import com.kmovie.security.CurrentUserProvider;
import com.kmovie.service.S3StorageService;
import com.kmovie.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.UUID;

@RestController
@RequestMapping("/accounts/profile/picture")
@RequiredArgsConstructor
public class ProfilePictureController {

    private final CurrentUserProvider currentUserProvider;
    private final UserService userService;
    private final FileRepository fileRepository;
    private final S3StorageService s3StorageService;

    @GetMapping
    public ResponseEntity<?> getOwnPicture() {
        User user = currentUserProvider.getUser();
        return servePicture(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPicture(@PathVariable UUID userId) {
        User user = userService.getById(userId);
        return servePicture(user);
    }

    private ResponseEntity<?> servePicture(User user) {
        if (user.getProfilePictureFileId() == null) {
            throw ApiException.notFound("This user has no profile picture");
        }
        FileEntity file = fileRepository.findByIdAndIsDeletedFalse(user.getProfilePictureFileId())
                .orElseThrow(() -> ApiException.notFound("Profile picture not found"));

        try {
            ResponseInputStream<GetObjectResponse> s3Stream = s3StorageService.getObject(file.getStorageKey(), null, null);
            MediaType mediaType = file.getContentType() != null
                    ? MediaType.parseMediaType(file.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(new InputStreamResource(s3Stream));
        } catch (NoSuchKeyException e) {
            throw ApiException.notFound("Profile picture file is missing from storage");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> setPicture(@RequestParam("file") MultipartFile file) {
        User user = currentUserProvider.getUser();
        userService.setProfilePicture(user, file);
        return ResponseEntity.ok(ApiResponse.ok("Profile picture updated"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deletePicture() {
        User user = currentUserProvider.getUser();
        userService.deleteProfilePicture(user);
        return ResponseEntity.ok(ApiResponse.ok("Profile picture deleted"));
    }
}
