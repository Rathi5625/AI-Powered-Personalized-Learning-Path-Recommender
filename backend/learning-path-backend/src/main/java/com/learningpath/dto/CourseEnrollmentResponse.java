package com.learningpath.dto;

import com.learningpath.entity.enums.ProgressStatus;

import java.time.Instant;
import java.util.UUID;

public record CourseEnrollmentResponse(
        UUID id,
        UUID userId,
        UUID courseId,
        String courseTitle,
        ProgressStatus status,
        Integer progressPercentage,
        Integer lastLessonCompleted,
        Integer totalLessons,
        Instant lastAccessedAt,
        Instant enrolledAt
) {
}
