package com.kmovie.service;

import com.kmovie.dto.request.ProfileUpdateRequest;
import com.kmovie.entity.FileEntity;
import com.kmovie.entity.User;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.FileRepository;
import com.kmovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3StorageService s3StorageService;

    public User getById(UUID id) {
        return userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional
    public User updateProfile(User user, ProfileUpdateRequest request) {
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
                throw ApiException.conflict("Username is already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPhoneNumberVerified(false);
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw ApiException.badRequest("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getSubscription() != null) {
            user.setSubscription(request.getSubscription());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(User user) {
        user.markDeleted();
        userRepository.save(user);
    }

    @Transactional
    public FileEntity setProfilePicture(User user, MultipartFile file) {
        FileEntity uploaded = s3StorageService.upload(file, "profile-pictures");
        user.setProfilePictureFileId(uploaded.getId());
        userRepository.save(user);
        return uploaded;
    }

    @Transactional
    public void deleteProfilePicture(User user) {
        if (user.getProfilePictureFileId() == null) {
            return;
        }
        fileRepository.findByIdAndIsDeletedFalse(user.getProfilePictureFileId()).ifPresent(f -> {
            s3StorageService.delete(f.getStorageKey());
            f.markDeleted();
            fileRepository.save(f);
        });
        user.setProfilePictureFileId(null);
        userRepository.save(user);
    }
}
