package com.learningpath.learningpath.dto;

import java.util.List;

public record LearningPathPhase(
        int phaseNumber,
        String phaseTitle,
        List<String> targetSkills,
        List<RecommendedCourseItem> courses,
        String estimatedDuration,
        String explanation
) {}
