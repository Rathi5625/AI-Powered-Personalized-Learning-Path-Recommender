package com.learningpath.ai.reasoning.dto;

import com.learningpath.recommendation.dto.SkillGapItemResponse;

import java.util.List;

public record GeminiReasoningInput(
        LearnerProfileDto learner,
        List<SkillGapItemResponse> skillGaps,
        List<CandidateCourseDto> candidateCourses,
        List<String> prerequisiteOrder
) {
}
