package com.kmovie.controller;

import com.kmovie.dto.request.LoginRequest;
import com.kmovie.dto.request.ProfileUpdateRequest;
import com.kmovie.dto.request.RegisterRequest;
import com.kmovie.dto.response.ApiResponse;
import com.kmovie.dto.response.LoginResponse;
import com.kmovie.dto.response.UserResponse;
import com.kmovie.entity.User;
import com.kmovie.security.CurrentUserProvider;
import com.kmovie.service.AuthService;
import com.kmovie.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AuthService authService;
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Please verify your email" +
                        (response.getPhoneNumber() != null ? " and phone number." : "."), response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        if (currentUserProvider.isAuthenticated()) {
            String jti = (String) request.getAttribute("jwt.jti");
            Date exp = (Date) request.getAttribute("jwt.exp");
            authService.logout(jti, exp);
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        User user = currentUserProvider.getUser();
        return ResponseEntity.ok(ApiResponse.ok(new UserResponse(user)));
    }

    @RequestMapping(value = "/profile", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User user = currentUserProvider.getUser();
        User updated = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", new UserResponse(updated)));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile() {
        User user = currentUserProvider.getUser();
        userService.deleteAccount(user);
        return ResponseEntity.ok(ApiResponse.ok("Account deleted"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String code) {
        User user = currentUserProvider.getUser();
        authService.verifyEmail(user, code);
        return ResponseEntity.ok(ApiResponse.ok("Email verified"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@RequestParam String code) {
        User user = currentUserProvider.getUser();
        authService.verifyPhoneNumber(user, code);
        return ResponseEntity.ok(ApiResponse.ok("Phone number verified"));
    }
}
