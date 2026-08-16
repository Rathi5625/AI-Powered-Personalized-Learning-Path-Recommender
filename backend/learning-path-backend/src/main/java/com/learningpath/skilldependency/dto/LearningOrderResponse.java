package com.learningpath.skilldependency.dto;

import java.util.List;

public record LearningOrderResponse(
        boolean success,
        List<String> learningOrder,
        List<String> unknownSkills,
        String error
) {
    public static LearningOrderResponse ok(List<String> learningOrder, List<String> unknownSkills) {
        return new LearningOrderResponse(true, learningOrder, unknownSkills, null);
    }

    public static LearningOrderResponse fail(String error) {
        return new LearningOrderResponse(false, List.of(), List.of(), error);
    }
}
