package com.kmovie.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private final CacheService cacheService;
    private final int otpLength;
    private final int expirationMinutes;
    private final SecureRandom random = new SecureRandom();

    public OtpService(CacheService cacheService,
                       @Value("${app.otp.length}") int otpLength,
                       @Value("${app.otp.expiration-minutes}") int expirationMinutes) {
        this.cacheService = cacheService;
        this.otpLength = otpLength;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateAndStore(String scopeKey) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        String code = sb.toString();
        cacheService.storeOtp(scopeKey, code, Duration.ofMinutes(expirationMinutes));
        return code;
    }

    public boolean verify(String scopeKey, String code) {
        String stored = cacheService.getOtp(scopeKey);
        boolean matches = stored != null && stored.equals(code);
        if (matches) {
            cacheService.clearOtp(scopeKey);
        }
        return matches;
    }
}
