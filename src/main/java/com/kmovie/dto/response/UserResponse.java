package com.kmovie.dto.response;

import com.kmovie.entity.User;
import com.kmovie.enums.Subscription;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class UserResponse {
    private final UUID id;
    private final String username;
    private final String email;
    private final String phoneNumber;
    private final boolean isAdmin;
    private final Subscription subscription;
    private final boolean isEmailVerified;
    private final boolean isPhoneNumberVerified;
    private final UUID profilePictureFileId;
    private final boolean isSuspended;
    private final LocalDateTime dateCreated;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.isAdmin = user.isAdmin();
        this.subscription = user.getSubscription();
        this.isEmailVerified = user.isEmailVerified();
        this.isPhoneNumberVerified = user.isPhoneNumberVerified();
        this.profilePictureFileId = user.getProfilePictureFileId();
        this.isSuspended = user.isSuspended();
        this.dateCreated = user.getDateCreated();
    }
}
