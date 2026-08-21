package com.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.ProjectStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String title,
        String description,
        List<String> technologies,
        CourseDifficulty difficulty,
        Integer estimatedHours,
        Integer milestonesCount,
        String repositoryTemplateUrl,
        ProjectStatus userStatus,
        Integer userProgressPercentage,
        Instant userStartedAt,
        Instant userCompletedAt
) {
}
