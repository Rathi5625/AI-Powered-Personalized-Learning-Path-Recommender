package com.learningpath.recommendation.dto;

import com.learningpath.entity.enums.RecommendationInteractionType;

import java.time.Instant;
import java.util.UUID;

public record RecommendationInteractionResponse(
        UUID id,
        UUID userId,
        UUID courseId,
        RecommendationInteractionType interactionType,
        Integer recommendationRank,
        Double ruleBasedScore,
        Double mlScore,
        Double finalScore,
        Instant createdAt
) {
}
