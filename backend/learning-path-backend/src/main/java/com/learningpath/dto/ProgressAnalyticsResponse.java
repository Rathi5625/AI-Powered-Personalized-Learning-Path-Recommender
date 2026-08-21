package com.learningpath.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProgressAnalyticsResponse(
        UUID userId,
        double totalLearningHours,
        int streakDays,
        int completedCoursesCount,
        int totalEnrolledCourses,
        int skillsMasteredCount,
        int totalAssessmentsTaken,
        double averageAssessmentScore,
        List<WeeklyActivityPoint> weeklyActivity,
        List<SkillProgressItem> skillProgressBreakdown,
        List<CourseEnrollmentResponse> recentCourses,
        List<AssessmentResultDto> assessmentHistory
) {
    public record WeeklyActivityPoint(String day, double hours, int lessonsCompleted) {}
    public record SkillProgressItem(UUID skillId, String skillName, String category, int proficiencyScore, String level) {}
}
