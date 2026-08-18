package com.learningpath.learningpath.dto;

import com.learningpath.entity.enums.LearningPathStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Summary DTO for displaying historical learning paths.
 */
public record LearningPathSummaryResponse(
        UUID pathId,
        UUID userId,
        String targetCareer,
        String title,
        String description,
        LearningPathStatus status,
        int totalPhases,
        int totalCourses,
        Instant createdAt,
        Instant updatedAt
) {
}
