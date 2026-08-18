package com.learningpath.ai.reasoning.dto;

import java.util.List;
import java.util.UUID;

public record GeminiCourseExplanationDto(
        UUID courseId,
        String reason,
        List<String> skillsAddressed,
        List<String> gapSkillsAddressed,
        String prerequisiteReason,
        String estimatedEffort,
        int priority
) {
}
