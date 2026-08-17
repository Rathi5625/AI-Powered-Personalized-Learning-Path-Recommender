package com.learningpath.learningpath.dto;

import com.learningpath.entity.enums.LearningPathStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO representing the learner's active persisted learning path.
 */
public record ActiveLearningPathResponse(
        UUID pathId,
        UUID userId,
        String targetCareer,
        String title,
        String description,
        LearningPathStatus status,
        int totalPhases,
        int totalCourses,
        List<LearningPathPhase> phases,
        Instant createdAt,
        Instant updatedAt
) {
}
