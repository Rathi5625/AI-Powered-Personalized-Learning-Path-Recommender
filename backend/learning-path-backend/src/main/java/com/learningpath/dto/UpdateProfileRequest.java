package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String fullName,

        @Size(max = 150, message = "Location must not exceed 150 characters")
        String location,

        @Size(max = 150, message = "Education must not exceed 150 characters")
        String education,

        Integer graduationYear,

        @Size(max = 200, message = "Current goal must not exceed 200 characters")
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

        PreferredContentType preferredContentType
) {
}
