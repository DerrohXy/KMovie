package com.kmovie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private LocalDateTime expiresAt;
    private UserResponse user;
}
