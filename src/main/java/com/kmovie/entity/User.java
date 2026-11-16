package com.kmovie.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kmovie.enums.Subscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription", nullable = false)
    private Subscription subscription = Subscription.BASIC;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(name = "is_phone_number_verified", nullable = false)
    private boolean isPhoneNumberVerified = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_picture_file_id")
    private FileEntity profilePictureFile;

    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended = false;
}
