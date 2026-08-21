package com.learningpath.adaptive.dto;

import com.learningpath.entity.enums.AdaptiveSessionStatus;
import com.learningpath.entity.enums.ConfidenceLevel;
import com.learningpath.entity.enums.CourseDifficulty;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdaptiveAssessmentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionStartResponse {
        private UUID sessionId;
        private UUID assessmentId;
        private String assessmentTitle;
        private String skillName;
        private CourseDifficulty currentDifficulty;
        private AdaptiveSessionStatus status;
        private Instant startedAt;
        private int totalAvailableQuestions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextQuestionResponse {
        private UUID sessionId;
        private String questionId;
        private int questionNumber;
        private int totalQuestionsEstimated;
        private String questionText;
        private String questionType;
        private List<String> options;
        private CourseDifficulty difficulty;
        private String skillName;
        private String conceptFocus;
        private boolean isTerminated;
        private String terminationReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerSubmissionRequest {
        private String questionId;
        private String answer;
        private int responseTimeSeconds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerSubmissionResult {
        private boolean correct;
        private String feedback;
        private String explanation;
        private double updatedKnowledgeProbability;
        private String updatedMasteryLevel;
        private CourseDifficulty nextDifficulty;
        private boolean possibleGuess;
        private boolean possibleCarelessError;
        private boolean sessionComplete;
        private String terminationReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionResultResponse {
        private UUID sessionId;
        private UUID assessmentId;
        private String assessmentTitle;
        private double overallScore;
        private double masteryEstimate;
        private double confidenceScore;
        private ConfidenceLevel confidenceLevel;
        private CourseDifficulty difficultyReached;
        private int questionsAnswered;
        private int correctAnswers;
        private int incorrectAnswers;
        private double averageResponseTimeSeconds;
        private List<String> strongSkills;
        private List<String> developingSkills;
        private List<String> weakSkills;
        private List<String> revisionRequired;
        private String behaviorCategory;
        private List<String> behaviorInsights;
        private String recommendedNextAction;
        private Map<String, Double> conceptCoverage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionAnalyticsResponse {
        private UUID sessionId;
        private List<Boolean> accuracyTrend;
        private List<String> difficultyProgression;
        private List<Integer> responseTimeTrend;
        private Map<String, Double> conceptMasteryDeltas;
        private int totalTimeSeconds;
        private String consistencyRating;
    }

    // Retain legacy inner classes for backward compatibility
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private String questionId;
        private String assessmentId;
        private String assessmentTitle;
        private String skillName;
        private String questionText;
        private String questionType;
        private List<String> options;
        private CourseDifficulty difficulty;
        private int questionNumber;
        private int totalQuestions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        private String questionId;
        private String answer;
        private int responseTimeSeconds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerResult {
        private boolean correct;
        private String feedback;
        private double updatedKnowledgeProbability;
        private com.learningpath.entity.enums.MasteryLevel updatedMasteryLevel;
        private CourseDifficulty nextRecommendedDifficulty;
        private boolean revisionSuggested;
        private String explanation;
    }
}
