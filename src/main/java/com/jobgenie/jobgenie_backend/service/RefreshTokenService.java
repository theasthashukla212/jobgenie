package com.jobgenie.jobgenie_backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobgenie.jobgenie_backend.exception.InvalidRefreshTokenException;
import com.jobgenie.jobgenie_backend.model.RefreshToken;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationMillis;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${app.refresh-token.expiration:2592000000}") long expirationMillis) {
        this.repository = repository;
        this.expirationMillis = expirationMillis;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(Instant.now().plus(expirationMillis, ChronoUnit.MILLIS));
        repository.save(token);
        return new IssuedRefreshToken(rawToken, expirationMillis / 1000);
    }

    @Transactional
    public User consume(String rawToken) {
        RefreshToken token = repository.findByTokenHashAndRevokedAtIsNull(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (token.getExpiresAt().isBefore(Instant.now()) || !token.getUser().isEnabled()) {
            token.setRevokedAt(Instant.now());
            throw new InvalidRefreshTokenException();
        }
        token.setRevokedAt(Instant.now());
        return token.getUser();
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record IssuedRefreshToken(String value, long expiresInSeconds) {}
}
