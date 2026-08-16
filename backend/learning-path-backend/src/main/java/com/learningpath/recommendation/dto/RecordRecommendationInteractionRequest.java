package com.learningpath.recommendation.dto;

import com.learningpath.entity.enums.RecommendationInteractionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecordRecommendationInteractionRequest(
        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "courseId is required")
        UUID courseId,

        @NotNull(message = "interactionType is required")
        RecommendationInteractionType interactionType,

        @Min(value = 1, message = "recommendationRank must be positive")
        Integer recommendationRank,

        @Min(value = 0, message = "ruleBasedScore must be >= 0")
        @Max(value = 100, message = "ruleBasedScore must be <= 100")
        Double ruleBasedScore,

        @Min(value = 0, message = "mlScore must be >= 0")
        @Max(value = 100, message = "mlScore must be <= 100")
        Double mlScore,

        @NotNull(message = "finalScore is required")
        @Min(value = 0, message = "finalScore must be >= 0")
        @Max(value = 100, message = "finalScore must be <= 100")
        Double finalScore
) {
}
