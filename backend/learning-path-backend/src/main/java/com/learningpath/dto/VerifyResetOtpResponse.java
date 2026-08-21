package com.learningpath.dto;

public record VerifyResetOtpResponse(
        String resetToken,
        String email,
        String message
) {
}
