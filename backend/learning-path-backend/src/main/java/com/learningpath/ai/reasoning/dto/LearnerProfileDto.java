package com.learningpath.ai.reasoning.dto;

public record LearnerProfileDto(
        String careerGoal,
        String experienceLevel,
        double dailyLearningHours,
        String learningStyle,
        String preferredContentType
) {
}
