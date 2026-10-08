package com.smartcampus.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(@Value("${app.security.jwt-secret:}") String configuredSecret,
                      @Value("${app.security.jwt-expiration-seconds:3600}") long expirationSeconds,
                      Environment environment) {
        if (expirationSeconds < 60 || expirationSeconds > 86_400) {
            throw new IllegalStateException("JWT expiration must be between 60 and 86400 seconds");
        }
        this.expirationSeconds = expirationSeconds;

        if (configuredSecret == null || configuredSecret.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod"))) {
                throw new IllegalStateException("JWT_SECRET must be configured for the prod profile");
            }
            byte[] developmentKey = new byte[32];
            new SecureRandom().nextBytes(developmentKey);
            this.signingKey = Keys.hmacShaKeyFor(developmentKey);
        } else {
            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(configuredSecret);
            } catch (IllegalArgumentException ignored) {
                keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            }
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes of key material");
            }
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String createToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String email = claims.getSubject();
        Role role = Role.valueOf(claims.get("role", String.class));
        return new AuthenticatedUser(email, role);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public record AuthenticatedUser(String email, Role role) {}
}
