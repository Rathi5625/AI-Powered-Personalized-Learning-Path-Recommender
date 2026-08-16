package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
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
}
