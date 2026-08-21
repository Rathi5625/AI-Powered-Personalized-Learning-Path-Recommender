package com.learningpath.ai.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class LearnerAiContext {
    UUID userId;
    String fullName;
    String targetCareer;
    String experienceLevel;
    String education;
    String personalObjective;
    Integer weeklyCommitmentHours;
    String learningStyle;
    String preferredContentType;
    String preferredLearningPace;

    List<LearnerSkillInfo> skills;
    List<LearnerAssessmentSummary> recentAssessments;
    List<LearnerCourseProgressInfo> activeCourses;
    List<String> completedCourseTitles;

    // BKT & Adaptive Modeling Signals
    double overallMasteryPercentage;
    List<String> masteredSkills;
    List<String> developingSkills;
    List<String> weakSkills;
    List<String> revisionRequiredSkills;
    double learningVelocity;
    double assessmentAccuracy;
    String preferredDifficulty;

    int activeStreakDays;
    double totalLearningHours;
    int profileCompletionPercentage;
    int careerReadinessScore;

    @Value
    @Builder
    public static class LearnerSkillInfo {
        String skillName;
        String proficiencyLevel;
        boolean verified;
    }

    @Value
    @Builder
    public static class LearnerAssessmentSummary {
        String title;
        String skillName;
        double scorePercentage;
        int timeSpentSeconds;
        String createdAt;
    }

    @Value
    @Builder
    public static class LearnerCourseProgressInfo {
        UUID courseId;
        String courseTitle;
        String provider;
        String difficulty;
        int progressPercentage;
        int timeSpentMinutes;
    }
}
