package com.shopway.auth.service;

import com.shopway.auth.dto.AuthResponse;
import com.shopway.auth.dto.LoginRequest;
import com.shopway.auth.dto.RegisterRequest;
import com.shopway.auth.entity.RefreshToken;
import com.shopway.auth.entity.User;
import com.shopway.auth.enums.Role;
import com.shopway.auth.repository.UserRepository;
import com.shopway.auth.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;


    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }


    /* =========================================================
       REGISTER
       ========================================================= */

    public AuthenticationResult register(
            RegisterRequest request
    ) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        if (userRepository.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        User user =
                new User(
                        request.firstName().trim(),
                        request.lastName().trim(),
                        email,
                        passwordEncoder.encode(
                                request.password()
                        ),
                        Role.USER
                );

        user =
                userRepository.save(user);

        return createAuthenticationResult(
                user
        );
    }


    /* =========================================================
       LOGIN
       ========================================================= */

    public AuthenticationResult login(
            LoginRequest request
    ) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        if (!user.isEnabled()) {

            throw new IllegalStateException(
                    "User account is disabled"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        return createAuthenticationResult(
                user
        );
    }


    /* =========================================================
       REFRESH
       ========================================================= */

    public AuthenticationResult refresh(
            String refreshToken
    ) {

        RefreshToken storedRefreshToken =
                refreshTokenService
                        .validateRefreshToken(
                                refreshToken
                        );

        User user =
                storedRefreshToken.getUser();

        /*
         * Revoke the old refresh token.
         *
         * This implements refresh-token rotation:
         *
         * old refresh token
         *        ↓
         *     revoked
         *        ↓
         * new refresh token
         */
        refreshTokenService
                .revokeRefreshToken(
                        refreshToken
                );

        return createAuthenticationResult(
                user
        );
    }


    /* =========================================================
       LOGOUT
       ========================================================= */

    public void logout(
            String refreshToken
    ) {

        refreshTokenService
                .revokeRefreshToken(
                        refreshToken
                );
    }


    /* =========================================================
       CREATE AUTHENTICATION RESULT
       ========================================================= */

    private AuthenticationResult
    createAuthenticationResult(
            User user
    ) {

        String accessToken =
                jwtService.generateToken(
                        user
                );

        String refreshToken =
                refreshTokenService
                        .createRefreshToken(
                                user
                        );

        AuthResponse authResponse =
                createAuthResponse(
                        user,
                        accessToken
                );

        return new AuthenticationResult(
                authResponse,
                refreshToken
        );
    }


    /* =========================================================
       CREATE AUTH RESPONSE
       ========================================================= */

    private AuthResponse createAuthResponse(
            User user,
            String accessToken
    ) {

        return new AuthResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}

