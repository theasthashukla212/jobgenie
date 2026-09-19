package com.jobgenie.jobgenie_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobgenie.jobgenie_backend.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}
