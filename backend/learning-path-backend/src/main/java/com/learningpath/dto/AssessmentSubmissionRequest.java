package com.learningpath.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record AssessmentSubmissionRequest(
        @NotNull(message = "Answers map is required")
        Map<UUID, String> answers, // questionId -> selectedOption

        Integer timeSpentSeconds
) {
}
