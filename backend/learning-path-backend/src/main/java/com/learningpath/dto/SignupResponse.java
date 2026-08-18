package com.learningpath.dto;

import java.util.UUID;

public record SignupResponse(
        UUID userId,
        String name,
        String email,
        String message
) {
}
