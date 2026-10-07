package com.shopway.auth.service;

import com.shopway.auth.dto.AuthResponse;

public record AuthenticationResult(

        AuthResponse authResponse,

        String refreshToken

) {
}
