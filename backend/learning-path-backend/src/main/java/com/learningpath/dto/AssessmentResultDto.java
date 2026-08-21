package com.learningpath.dto;

import com.learningpath.entity.enums.ProficiencyLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssessmentResultDto(
        UUID id,
        UUID assessmentId,
        String assessmentTitle,
        String skillName,
        BigDecimal score,
        boolean passed,
        ProficiencyLevel evaluatedProficiency,
        int totalQuestions,
        int correctAnswers,
        Integer timeSpentSeconds,
        Instant completedAt
) {
}
