package com.learningpath.dto;

import com.learningpath.entity.enums.ProgressStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LearningProgressResponse(
        UUID progressId,
        UUID userId,
        UUID courseId,
        String courseTitle,
        ProgressStatus status,
        BigDecimal completionPercentage,
        Instant lastAccessedAt,
        Instant updatedAt
) {}
