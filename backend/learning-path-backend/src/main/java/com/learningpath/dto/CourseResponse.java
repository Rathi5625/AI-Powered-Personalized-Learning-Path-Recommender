package com.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        String provider,
        String url,
        CourseDifficulty difficulty,
        Double durationHours,
        CourseType courseType,
        String language,
        BigDecimal rating,
        BigDecimal price,
        boolean isFree,
        Instant createdAt,
        Instant updatedAt
) {
}
