package com.learningpath.dto;

import com.learningpath.entity.enums.UserRole;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String email,
        UserRole role
) {
    public UserSummaryResponse(UUID id, String name, String email) {
        this(id, name, email, UserRole.USER);
    }
}
