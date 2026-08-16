package com.learningpath.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationSummaryResponse(
        UUID userId,
        String userName,
        UUID careerId,
        String careerName,
        boolean hasGaps,
        int totalCandidateCourses,
        List<CourseRecommendationResponse> recommendations
) {
}
