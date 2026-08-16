package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserSkillResponse(
        UUID id,
        UUID userId,
        UUID skillId,
        String skillName,
        String skillCategory,
        ProficiencyLevel proficiencyLevel,
        BigDecimal confidence,
        SkillSource source,
        boolean isVerified,
        Instant lastAssessedDate,
        Instant createdAt,
        Instant updatedAt
) {
}
