package com.learningpath.dto;

import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;

import java.util.List;

public record DashboardResponse(
        UserResponse user,
        ActiveLearningPathResponse activeLearningPath,
        UserProgressSummaryResponse progressSummary,
        SkillGapSummaryResponse skillGapSummary,
        List<CourseRecommendationResponse> topRecommendations
) {}
