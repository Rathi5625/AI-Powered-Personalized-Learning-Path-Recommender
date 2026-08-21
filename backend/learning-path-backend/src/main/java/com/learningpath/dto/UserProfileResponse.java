package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        boolean emailVerified,
        String location,
        String education,
        Integer graduationYear,
        String currentGoal,
        String personalObjective,
        String bio,
        String avatarUrl,
        String githubUrl,
        String linkedinUrl,
        String portfolioUrl,
        String targetCareer,
        ExperienceLevel experienceLevel,
        Integer dailyLearningHours,
        Integer weeklyCommitmentHours,
        String preferredLearningPace,
        String availableDays,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType,
        int profileCompletionPercentage,
        List<UserSkillResponse> skills,
        Instant createdAt,
        Instant updatedAt
) {
}
