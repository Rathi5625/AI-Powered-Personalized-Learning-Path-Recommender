package com.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String courseCode,
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
        String youtubeTitle,
        String youtubeUrl,
        String youtubeNotes,
        Instant createdAt,
        Instant updatedAt
) {
    // Backward compatibility constructor for existing calls & tests
    public CourseResponse(
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
        this(id, null, title, description, provider, url, difficulty, durationHours, courseType, language, rating, price, isFree, null, null, null, createdAt, updatedAt);
    }
}
