package com.learningpath.ai.reasoning.dto;

import java.util.List;

public record GeminiReasoningResult(
        String summary,
        List<GeminiCourseExplanationDto> recommendations,
        List<GeminiCourseSequenceItemDto> learningSequence,
        List<String> adaptationNotes,
        boolean isAiGenerated
) {
}
