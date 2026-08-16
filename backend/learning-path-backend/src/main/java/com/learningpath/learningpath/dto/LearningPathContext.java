package com.learningpath.learningpath.dto;

import java.util.List;
import java.util.UUID;

public record LearningPathContext(
        UUID userId,
        String fullName,
        String targetCareer,
        String experienceLevel,
        String learningStyle,
        Double dailyLearningHours,
        List<String> currentSkills,
        List<String> skillGaps,
        List<String> orderedTargetSkills,
        List<RecommendedCourseItem> candidateCourses
) {}
