package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        String targetCareer,
        ExperienceLevel experienceLevel,
        Integer dailyLearningHours,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType
) {
}
