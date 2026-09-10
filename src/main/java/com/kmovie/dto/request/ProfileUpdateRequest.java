package com.kmovie.dto.request;

import com.kmovie.enums.Subscription;
import lombok.Getter;
import lombok.Setter;

/** All fields optional; only non-null fields are applied. */
@Getter
@Setter
public class ProfileUpdateRequest {
    private String username;
    private String phoneNumber;
    private String currentPassword;
    private String newPassword;
    private Subscription subscription;
}
