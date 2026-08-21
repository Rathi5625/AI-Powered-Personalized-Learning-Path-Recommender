package com.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.QuestionType;

import java.util.List;
import java.util.UUID;

public record AssessmentQuestionDto(
        UUID id,
        String questionText,
        QuestionType questionType,
        CourseDifficulty difficulty,
        List<String> options,
        Integer points
) {
}
