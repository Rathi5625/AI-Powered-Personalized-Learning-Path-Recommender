package com.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CourseRequest(
        String courseCode,

        @NotBlank(message = "Course title must not be blank")
        String title,

        String description,

        @NotBlank(message = "Provider must not be blank")
        String provider,

        String url,

        @NotNull(message = "Difficulty must not be null")
        CourseDifficulty difficulty,

        @Positive(message = "Duration in hours must be positive")
        Double durationHours,

        CourseType courseType,

        String language,

        @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
        @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
        BigDecimal rating,

        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal price,

        Boolean isFree,

        String youtubeTitle,

        String youtubeUrl,

        String youtubeNotes
) {
    // Backward compatibility constructor for existing tests and client requests
    public CourseRequest(
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
            Boolean isFree
    ) {
        this(null, title, description, provider, url, difficulty, durationHours, courseType, language, rating, price, isFree, null, null, null);
    }
}
