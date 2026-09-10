package com.kmovie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Thin wrapper around Redis for the two things this app needs a cache for:
 * blacklisting logged-out JWTs until they naturally expire, and short-lived
 * OTP codes for email/phone verification. Also exposed as a general purpose
 * get/set/delete/exists cache for anything else that benefits from it
 * (e.g. title listings).
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String OTP_PREFIX = "otp:";

    public void set(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ---- JWT blacklist (used by /accounts/logout) ----

    public void blacklistToken(String jti, Duration ttl) {
        if (ttl.isNegative() || ttl.isZero()) {
            return; // already expired, nothing to blacklist
        }
        set(BLACKLIST_PREFIX + jti, "1", ttl);
    }

    public boolean isTokenBlacklisted(String jti) {
        return exists(BLACKLIST_PREFIX + jti);
    }

    // ---- OTP codes (used by email/phone verification) ----

    public void storeOtp(String key, String code, Duration ttl) {
        set(OTP_PREFIX + key, code, ttl);
    }

    public String getOtp(String key) {
        return get(OTP_PREFIX + key);
    }

    public void clearOtp(String key) {
        delete(OTP_PREFIX + key);
    }
}
