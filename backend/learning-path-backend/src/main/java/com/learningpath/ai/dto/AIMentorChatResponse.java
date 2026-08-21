package com.learningpath.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMentorChatResponse {
    private String messageId;
    private String conversationId;
    private String role; // "mentor"
    private String reply;
    private String topic;
    private Double confidenceScore;
    private String recommendedAction;
    private List<String> suggestedFollowUps;
    private List<MentorActionDto> actions;
    private List<RecommendedResourceDto> recommendedResources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentorActionDto {
        private String type; // "START_ASSESSMENT", "VIEW_LEARNING_PLAN", "REVISE_TOPIC", "EXPLORE_COURSE"
        private String label;
        private String targetUrl;
        private String targetId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedResourceDto {
        private String title;
        private String type;
        private String difficulty;
        private String url;
        private Double matchScore;
    }
}
