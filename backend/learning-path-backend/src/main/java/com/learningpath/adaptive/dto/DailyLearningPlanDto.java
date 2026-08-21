package com.learningpath.adaptive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLearningPlanDto {
    private String title;
    private String targetCareer;
    private int estimatedTotalMinutes;
    private String focusTopic;
    private double currentMasteryProbability;
    private List<PlanItemDto> items;
    private String generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanItemDto {
        private String id;
        private String title;
        private String type; // "LEARN", "PRACTICE", "ASSESSMENT", "REVISION"
        private int durationMinutes;
        private String difficulty;
        private String reason;
        private String actionUrl;
        private int priority; // 1 = highest
    }
}
