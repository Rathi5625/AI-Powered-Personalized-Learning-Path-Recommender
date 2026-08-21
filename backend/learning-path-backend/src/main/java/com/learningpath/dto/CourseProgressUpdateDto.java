package com.learningpath.dto;

import com.learningpath.entity.enums.ProgressStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CourseProgressUpdateDto(
        @NotNull(message = "Progress status is required")
        ProgressStatus status,

        @Min(value = 0, message = "Progress must be at least 0")
        @Max(value = 100, message = "Progress must not exceed 100")
        Integer progressPercentage,

        Integer lastLessonCompleted,

        Integer timeSpentMinutes
) {
}
