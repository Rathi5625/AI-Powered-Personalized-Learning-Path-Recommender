package com.learningpath.recommendation.dto;

public record UserInteractionStatsResponse(
        long totalInteractions,
        long viewed,
        long clicked,
        long started,
        long completed,
        long liked,
        long skipped
) {
}
