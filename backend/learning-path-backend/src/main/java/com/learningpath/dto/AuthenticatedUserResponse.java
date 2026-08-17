package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;

import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUserResponse(
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
}
