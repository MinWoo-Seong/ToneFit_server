package com.example.tonefitserver.core.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
