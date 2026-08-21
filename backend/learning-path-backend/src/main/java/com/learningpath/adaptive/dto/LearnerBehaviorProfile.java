package com.learningpath.adaptive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerBehaviorProfile {
    private String preferredDifficulty;
    private double learningVelocity;
    private double consistency;
    private double assessmentAccuracy;
    private double revisionNeed;
    private int preferredSessionLengthMinutes;
    private String strongestLearningFormat;
    private int activeStreakDays;
    private int totalSessionsRecorded;
    private boolean insufficientData;
    private String dataQualityStatus; // "COMPLETE", "PARTIAL", "INSUFFICIENT_DATA"
    private String behaviorCategory;
    private java.util.List<String> behaviorInsights;
}

