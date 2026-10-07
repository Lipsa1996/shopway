package com.shopway.auth.repository;

import com.shopway.auth.entity.RefreshToken;
import com.shopway.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findAllByUser(
            User user
    );

    void deleteAllByUser(
            User user
    );
}

