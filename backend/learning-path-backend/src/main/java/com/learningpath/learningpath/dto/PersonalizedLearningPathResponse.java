package com.learningpath.learningpath.dto;

import java.util.List;
import java.util.UUID;

public record PersonalizedLearningPathResponse(
        boolean success,
        UUID userId,
        String targetCareer,
        String summary,
        List<LearningPathPhase> phases,
        String provider,
        String model,
        String error
) {
    public static PersonalizedLearningPathResponse ok(
            UUID userId,
            String targetCareer,
            String summary,
            List<LearningPathPhase> phases,
            String provider,
            String model
    ) {
        return new PersonalizedLearningPathResponse(true, userId, targetCareer, summary, phases, provider, model, null);
    }

    public static PersonalizedLearningPathResponse fail(UUID userId, String targetCareer, String error) {
        return new PersonalizedLearningPathResponse(false, userId, targetCareer, null, List.of(), null, null, error);
    }

    public static PersonalizedLearningPathResponse fail(String error) {
        return new PersonalizedLearningPathResponse(false, null, null, null, List.of(), null, null, error);
    }
}
