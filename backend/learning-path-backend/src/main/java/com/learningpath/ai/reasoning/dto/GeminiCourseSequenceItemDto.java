package com.learningpath.ai.reasoning.dto;

import java.util.UUID;

public record GeminiCourseSequenceItemDto(
        UUID courseId,
        int order,
        String reason
) {
}
