package com.learningpath.dto;

public record AdminTestResponse(
        String role,
        String message,
        String email
) {
}
