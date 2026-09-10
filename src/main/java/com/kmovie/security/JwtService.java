package com.kmovie.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long defaultExpirationDays;
    private final long stayLoggedInExpirationDays;
    private final String issuer;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-days}") long defaultExpirationDays,
                       @Value("${app.jwt.stay-logged-in-expiration-days}") long stayLoggedInExpirationDays,
                       @Value("${app.jwt.issuer}") String issuer) {
        // Secret is expected to be at least 256 bits for HS256; pad defensively if a short value is supplied.
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.defaultExpirationDays = defaultExpirationDays;
        this.stayLoggedInExpirationDays = stayLoggedInExpirationDays;
        this.issuer = issuer;
    }

    public String generateToken(UUID userId, String username, boolean isAdmin, boolean stayLoggedIn) {
        Instant now = Instant.now();
        long days = stayLoggedIn ? stayLoggedInExpirationDays : defaultExpirationDays;
        Instant expiry = now.plus(days, ChronoUnit.DAYS);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(userId.toString())
                .claim("username", username)
                .claim("isAdmin", isAdmin)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }

    public Date getExpiration(Claims claims) {
        return claims.getExpiration();
    }
}
