package com.learningpath.dto;

public record UserProgressSummaryResponse(
        int totalCoursesTracked,
        int completedCourses,
        int inProgressCourses,
        int notStartedCourses,
        int pausedCourses,
        double overallCompletionRate
) {}
