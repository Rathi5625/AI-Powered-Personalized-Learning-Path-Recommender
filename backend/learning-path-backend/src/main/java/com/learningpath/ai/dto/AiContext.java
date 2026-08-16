package com.learningpath.ai.dto;

import java.util.List;
import java.util.UUID;

public record AiContext(
        UUID learnerId,
        UUID careerId,
        String careerName,
        String learnerGoals,
        String learningPreferences,
        Double availableStudyHours,
        List<String> currentSkills,
        List<String> skillGaps,
        List<String> recommendedCourses
) {}
