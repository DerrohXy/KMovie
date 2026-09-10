package com.kmovie.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kmovie.enums.Subscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subscription subscription = Subscription.BASIC;

    @Column(nullable = false)
    private boolean isEmailVerified = false;

    @Column(nullable = false)
    private boolean isPhoneNumberVerified = false;

    private UUID profilePictureFileId;

    @Column(nullable = false)
    private boolean isSuspended = false;
}
