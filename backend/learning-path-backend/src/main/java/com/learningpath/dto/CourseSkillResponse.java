package com.learningpath.dto;

import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;

import java.time.Instant;
import java.util.UUID;

public record CourseSkillResponse(
        UUID id,
        UUID courseId,
        String courseTitle,
        UUID skillId,
        String skillName,
        String skillCategory,
        CoverageLevel coverageLevel,
        SkillPriority importance,
        ProficiencyLevel targetProficiency,
        boolean isPrimarySkill,
        Instant createdAt,
        Instant updatedAt
) {
}
