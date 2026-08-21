package com.learningpath.dto;

import java.util.List;
import java.util.UUID;

public record AssessmentDto(
        UUID id,
        String title,
        String description,
        UUID skillId,
        String skillName,
        Integer passingScore,
        int questionCount,
        Integer estimatedMinutes,
        List<AssessmentQuestionDto> questions
) {
}
