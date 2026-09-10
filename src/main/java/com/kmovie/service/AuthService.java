package com.kmovie.service;

import com.kmovie.dto.request.LoginRequest;
import com.kmovie.dto.request.RegisterRequest;
import com.kmovie.dto.response.LoginResponse;
import com.kmovie.dto.response.UserResponse;
import com.kmovie.entity.User;
import com.kmovie.enums.Subscription;
import com.kmovie.exception.ApiException;
import com.kmovie.repository.UserRepository;
import com.kmovie.security.JwtService;
import com.kmovie.util.PhoneNumberValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PhoneNumberValidator phoneNumberValidator;
    private final EmailService emailService;
    private final SmsService smsService;
    private final OtpService otpService;
    private final CacheService cacheService;

    @Value("${app.jwt.expiration-days}")
    private long defaultExpirationDays;

    @Value("${app.jwt.stay-logged-in-expiration-days}")
    private long stayLoggedInExpirationDays;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw ApiException.conflict("Email is already registered");
        }
        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw ApiException.conflict("Username is already taken");
        }

        String normalizedPhone = null;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (!phoneNumberValidator.isValid(request.getPhoneNumber())) {
                throw ApiException.badRequest("Invalid phone number");
            }
            normalizedPhone = phoneNumberValidator.normalize(request.getPhoneNumber());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(normalizedPhone);
        user.setAdmin(false);
        user.setSubscription(Subscription.BASIC);
        user = userRepository.save(user);

        // Queue email verification
        String emailOtp = otpService.generateAndStore("email-verify:" + user.getId());
        emailService.queueEmailVerification(user.getEmail(), emailOtp);

        // Queue phone verification if a phone number was supplied
        if (normalizedPhone != null) {
            String phoneOtp = otpService.generateAndStore("phone-verify:" + user.getId());
            smsService.queuePhoneVerification(normalizedPhone, phoneOtp);
        }

        return new UserResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> ApiException.unauthorized("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid username/email or password");
        }
        if (user.isSuspended()) {
            throw ApiException.forbidden("This account has been suspended");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.isAdmin(), request.isStayLoggedIn());
        Claims claims = jwtService.parseClaims(token);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());

        return new LoginResponse(token, "Bearer", expiresAt, new UserResponse(user));
    }

    /** Blacklists the current JWT (by jti) for the remainder of its natural lifetime. */
    public void logout(String jti, java.util.Date expiration) {
        if (jti == null) {
            return;
        }
        Duration ttl = expiration != null
                ? Duration.between(Instant.now(), expiration.toInstant())
                : Duration.ofDays(stayLoggedInExpirationDays);
        cacheService.blacklistToken(jti, ttl);
    }

    public boolean verifyEmail(User user, String code) {
        boolean ok = otpService.verify("email-verify:" + user.getId(), code);
        if (!ok) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired verification code");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        return true;
    }

    public boolean verifyPhoneNumber(User user, String code) {
        boolean ok = otpService.verify("phone-verify:" + user.getId(), code);
        if (!ok) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired verification code");
        }
        user.setPhoneNumberVerified(true);
        userRepository.save(user);
        return true;
    }
}
