package com.learningpath.dto;

import com.learningpath.entity.enums.SkillDifficulty;

import java.time.Instant;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        String category,
        String description,
        SkillDifficulty difficulty,
        Instant createdAt,
        Instant updatedAt
) {
}
