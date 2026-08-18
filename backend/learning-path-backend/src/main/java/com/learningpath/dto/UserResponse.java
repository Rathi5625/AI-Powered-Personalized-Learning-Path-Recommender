package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserRole role,
        String careerGoal,
        ExperienceLevel experienceLevel,
        Integer dailyLearningHours,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType,
        Instant createdAt,
        Instant updatedAt
) {
    public UserResponse(
            UUID id,
            String name,
            String email,
            String careerGoal,
            ExperienceLevel experienceLevel,
            Integer dailyLearningHours,
            LearningStyle learningStyle,
            PreferredContentType preferredContentType,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, name, email, UserRole.USER, careerGoal, experienceLevel, dailyLearningHours, learningStyle, preferredContentType, createdAt, updatedAt);
    }
}
