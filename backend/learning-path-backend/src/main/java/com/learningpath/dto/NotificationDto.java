package com.learningpath.dto;

import com.learningpath.entity.enums.NotificationCategory;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String title,
        String message,
        NotificationCategory category,
        boolean read,
        String actionUrl,
        Instant createdAt
) {
}
