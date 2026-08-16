package com.learningpath.dto;

import com.learningpath.entity.enums.ProgressStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LearningProgressRequest(

        @NotNull(message = "Progress status is required")
        ProgressStatus status,

        @DecimalMin(value = "0", message = "Completion percentage must be at least 0")
        @DecimalMax(value = "100", message = "Completion percentage must be at most 100")
        BigDecimal completionPercentage
) {}
