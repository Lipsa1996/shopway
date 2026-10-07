package com.shopway.auth.controller;

import com.shopway.auth.dto.AuthResponse;
import com.shopway.auth.dto.LoginRequest;
import com.shopway.auth.dto.RegisterRequest;
import com.shopway.auth.service.AuthService;
import com.shopway.auth.service.AuthenticationResult;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE =
            "refresh_token";

    private static final String COOKIE_PATH =
            "/api/auth";

    private static final long REFRESH_TOKEN_EXPIRATION_DAYS =
            7;

    private final AuthService authService;


    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }


    /* =========================================================
       REGISTER
       ========================================================= */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthenticationResult result =
                authService.register(request);

        ResponseCookie refreshCookie =
                createRefreshTokenCookie(
                        result.refreshToken()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        result.authResponse()
                );
    }


    /* =========================================================
       LOGIN
       ========================================================= */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthenticationResult result =
                authService.login(request);

        ResponseCookie refreshCookie =
                createRefreshTokenCookie(
                        result.refreshToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        result.authResponse()
                );
    }


    /* =========================================================
       REFRESH ACCESS TOKEN
       ========================================================= */

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String refreshToken
    ) {

        if (
                refreshToken == null ||
                        refreshToken.isBlank()
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        AuthenticationResult result =
                authService.refresh(
                        refreshToken
                );

        ResponseCookie refreshCookie =
                createRefreshTokenCookie(
                        result.refreshToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        result.authResponse()
                );
    }


    /* =========================================================
       LOGOUT
       ========================================================= */

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String refreshToken
    ) {

        authService.logout(
                refreshToken
        );

        ResponseCookie deleteCookie =
                deleteRefreshTokenCookie();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        deleteCookie.toString()
                )
                .build();
    }


    /* =========================================================
       CREATE REFRESH TOKEN COOKIE
       ========================================================= */

    private ResponseCookie createRefreshTokenCookie(
            String refreshToken
    ) {

        return ResponseCookie
                .from(
                        REFRESH_TOKEN_COOKIE,
                        refreshToken
                )
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(
                        Duration.ofDays(
                                REFRESH_TOKEN_EXPIRATION_DAYS
                        )
                )
                .build();
    }


    /* =========================================================
       DELETE REFRESH TOKEN COOKIE
       ========================================================= */

    private ResponseCookie deleteRefreshTokenCookie() {

        return ResponseCookie
                .from(
                        REFRESH_TOKEN_COOKIE,
                        ""
                )
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }
}
