package com.learningpath.dto;

import java.time.Instant;
import java.util.UUID;

public record CareerResponse(
        UUID id,
        String name,
        String description,
        String category,
        Instant createdAt,
        Instant updatedAt
) {
}
