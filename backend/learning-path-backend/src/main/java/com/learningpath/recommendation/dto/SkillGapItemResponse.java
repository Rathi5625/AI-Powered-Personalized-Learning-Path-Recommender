package com.learningpath.recommendation.dto;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;

import java.util.UUID;

public record SkillGapItemResponse(
        UUID skillId,
        String skillName,
        String skillCategory,
        String currentProficiency,
        ProficiencyLevel requiredProficiency,
        GapType gapType,
        GapSeverity severity,
        SkillPriority priority,
        boolean mandatory,
        String explanation
) {
}
