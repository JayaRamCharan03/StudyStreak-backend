package com.studystreak.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Creates and reads the login token. The token holds the user's email. */
@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expiryMillis;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                @Value("${app.jwt.expiry-hours:168}") long expiryHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMillis = expiryHours * 3_600_000L;
    }

    public String create(String email) {
        Date now = new Date();
        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expiryMillis))
            .signWith(key)
            .compact();
    }

    /** Returns the email inside the token, or throws if the token is fake or expired. */
    public String parse(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }
}