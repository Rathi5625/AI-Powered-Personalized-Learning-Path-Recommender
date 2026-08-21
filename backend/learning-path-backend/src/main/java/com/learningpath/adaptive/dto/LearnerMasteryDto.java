package com.learningpath.adaptive.dto;

import com.learningpath.entity.enums.MasteryLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class LearnerMasteryDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalConceptsTracked;
        private double overallMasteryPercentage;
        private List<String> masteredSkills;
        private List<String> developingSkills;
        private List<String> weakSkills;
        private List<String> revisionRequiredSkills;
        private List<ConceptItem> conceptStates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConceptItem {
        private String id;
        private String conceptName;
        private String skillId;
        private double knowledgeProbability;
        private MasteryLevel masteryLevel;
        private int attempts;
        private int correctAttempts;
        private double confidenceScore;
        private boolean revisionRequired;
        private String lastAttemptAt;
    }
}
