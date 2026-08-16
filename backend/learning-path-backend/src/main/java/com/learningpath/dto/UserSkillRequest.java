package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UserSkillRequest(
        @NotNull(message = "Skill ID must not be null")
        UUID skillId,

        @NotNull(message = "Proficiency level must not be null")
        ProficiencyLevel proficiencyLevel,

        @DecimalMin(value = "0.0", message = "Confidence must be at least 0.0")
        @DecimalMax(value = "100.0", message = "Confidence must not exceed 100.0")
        BigDecimal confidence,

        SkillSource source
) {
}
