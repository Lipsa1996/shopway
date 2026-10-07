package com.shopway.auth.service;

import com.shopway.auth.entity.RefreshToken;
import com.shopway.auth.entity.User;
import com.shopway.auth.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshTokenExpirationDays;

    private final SecureRandom secureRandom =
            new SecureRandom();


    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration-days:7}")
            long refreshTokenExpirationDays
    ) {
        this.refreshTokenRepository =
                refreshTokenRepository;

        this.refreshTokenExpirationDays =
                refreshTokenExpirationDays;
    }


    /**
     * Creates a new refresh token for the user.
     *
     * The raw token is returned only to the caller.
     * Only its SHA-256 hash is stored in the database.
     */
    public String createRefreshToken(
            User user
    ) {

        String rawToken =
                generateToken();

        String tokenHash =
                hashToken(rawToken);

        Instant expiresAt =
                Instant.now()
                        .plus(
                                refreshTokenExpirationDays,
                                ChronoUnit.DAYS
                        );

        RefreshToken refreshToken =
                new RefreshToken(
                        tokenHash,
                        user,
                        expiresAt
                );

        refreshTokenRepository.save(
                refreshToken
        );

        return rawToken;
    }


    /**
     * Validates the supplied raw refresh token
     * and returns the associated database entity.
     */
    public RefreshToken validateRefreshToken(
            String rawToken
    ) {

        if (
                rawToken == null ||
                        rawToken.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Refresh token is missing"
            );
        }

        String tokenHash =
                hashToken(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {

            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (
                refreshToken
                        .getExpiresAt()
                        .isBefore(Instant.now())
        ) {

            refreshToken.setRevoked(true);

            refreshTokenRepository.save(
                    refreshToken
            );

            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        if (
                !refreshToken
                        .getUser()
                        .isEnabled()
        ) {

            throw new IllegalStateException(
                    "User account is disabled"
            );
        }

        return refreshToken;
    }


    /**
     * Revokes a refresh token.
     */
    public void revokeRefreshToken(
            String rawToken
    ) {

        if (
                rawToken == null ||
                        rawToken.isBlank()
        ) {
            return;
        }

        String tokenHash =
                hashToken(rawToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {

                    refreshToken.setRevoked(true);

                    refreshTokenRepository.save(
                            refreshToken
                    );
                });
    }


    /**
     * Generates a cryptographically secure
     * random refresh token.
     */
    private String generateToken() {

        byte[] randomBytes =
                new byte[64];

        secureRandom.nextBytes(
                randomBytes
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        randomBytes
                );
    }


    /**
     * Creates a SHA-256 hash of the raw
     * refresh token before database storage.
     */
    private String hashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }
}

