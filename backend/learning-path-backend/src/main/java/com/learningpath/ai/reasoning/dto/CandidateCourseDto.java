package com.learningpath.ai.reasoning.dto;

import java.util.List;
import java.util.UUID;

public record CandidateCourseDto(
        UUID courseId,
        String courseCode,
        String title,
        String provider,
        String difficulty,
        String courseType,
        List<String> skillsCovered,
        List<String> gapSkillsAddressed,
        double ruleScore,
        Double mlScore,
        double finalScore
) {
}
