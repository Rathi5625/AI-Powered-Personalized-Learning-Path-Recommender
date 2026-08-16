package com.learningpath.recommendation.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseRecommendationResponse(
        int rank,
        UUID courseId,
        String courseTitle,
        String provider,
        String url,
        CourseDifficulty difficulty,
        CourseType courseType,
        BigDecimal rating,
        BigDecimal price,
        boolean isFree,
        Double ruleBasedScore,
        Double mlScore,
        double finalScore,
        List<String> matchedSkills,
        List<String> gapSkillsAddressed,
        String explanation
) {
}
