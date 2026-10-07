package com.shopway.auth.dto;

public record AuthResponse(

        String accessToken,

        String tokenType,

        Long userId,

        String firstName,

        String lastName,

        String email,

        String role

) {
}