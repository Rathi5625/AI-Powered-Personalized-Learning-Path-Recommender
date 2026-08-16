package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;

import java.time.Instant;
import java.util.UUID;

public record CareerSkillResponse(
        UUID id,
        UUID careerId,
        String careerName,
        UUID skillId,
        String skillName,
        String skillCategory,
        SkillPriority priority,
        ProficiencyLevel requiredProficiency,
        boolean isMandatory,
        Instant createdAt,
        Instant updatedAt
) {
}
