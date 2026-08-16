package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import jakarta.validation.constraints.NotNull;

public record CareerSkillUpdateRequest(
        @NotNull(message = "Priority must not be null")
        SkillPriority priority,

        @NotNull(message = "Required proficiency must not be null")
        ProficiencyLevel requiredProficiency,

        Boolean isMandatory
) {
}
