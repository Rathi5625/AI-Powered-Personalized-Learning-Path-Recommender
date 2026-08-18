package com.learningpath.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummaryResponse user
) {
}
