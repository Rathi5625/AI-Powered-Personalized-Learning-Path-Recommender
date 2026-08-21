package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.learningpath.dto.ActiveLearningPathResponse;

import java.util.List;
import java.util.UUID;

public record DashboardAggregatedResponse(
        UUID userId,
        String userName,
        String targetCareer,
        ExperienceLevel experienceLevel,
        int profileCompletionPercentage,
        int activeStreakDays,
        double totalLearningHours,
        int completedCoursesCount,
        int inProgressCoursesCount,
        int totalSkillsCount,
        int unreadNotificationsCount,
        ActiveLearningPathResponse activeLearningPath,
        List<CourseEnrollmentResponse> enrolledCourses,
        List<UserSkillResponse> topSkills,
        List<AssessmentResultDto> recentAssessments,
        List<ProjectDto> activeProjects,
        List<NotificationDto> recentNotifications
) {
}
