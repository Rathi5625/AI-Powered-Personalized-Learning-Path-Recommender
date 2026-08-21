package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String targetCareer,
        ExperienceLevel experienceLevel,
        Integer dailyLearningHours,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType,
        Boolean emailVerified,
        Boolean onboardingCompleted,
        Instant createdAt,
        Instant updatedAt
) {
    public AuthenticatedUserResponse(
            UUID id,
            String name,
            String email,
            String targetCareer,
            ExperienceLevel experienceLevel,
            Integer dailyLearningHours,
            LearningStyle learningStyle,
            PreferredContentType preferredContentType,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                name,
                email,
                UserRole.USER,
                targetCareer,
                experienceLevel,
                dailyLearningHours,
                learningStyle,
                preferredContentType,
                true,
                (targetCareer != null && !targetCareer.isBlank() && experienceLevel != null),
                createdAt,
                updatedAt
        );
    }
}
