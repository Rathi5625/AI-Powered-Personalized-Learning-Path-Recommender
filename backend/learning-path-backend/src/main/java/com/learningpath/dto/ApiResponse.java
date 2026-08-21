package com.learningpath.dto;

public record ApiResponse(
        boolean success,
        String message
) {
}
