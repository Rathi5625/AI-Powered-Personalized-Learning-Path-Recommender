package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CareerSkillRequest(
        @NotNull(message = "Skill ID must not be null")
        UUID skillId,

        @NotNull(message = "Priority must not be null")
        SkillPriority priority,

        @NotNull(message = "Required proficiency must not be null")
        ProficiencyLevel requiredProficiency,

        Boolean isMandatory
) {
}
