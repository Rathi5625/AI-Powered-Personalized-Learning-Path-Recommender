package com.learningpath.dto;

import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CourseSkillRequest(
        @NotNull(message = "Skill ID must not be null")
        UUID skillId,

        @NotNull(message = "Coverage level must not be null")
        CoverageLevel coverageLevel,

        @NotNull(message = "Importance must not be null")
        SkillPriority importance,

        ProficiencyLevel targetProficiency,

        Boolean isPrimarySkill
) {
}
