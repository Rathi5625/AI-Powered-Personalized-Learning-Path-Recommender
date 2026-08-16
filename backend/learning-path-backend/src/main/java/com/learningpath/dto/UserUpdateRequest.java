package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "Name must not be blank")
        String name,

        String careerGoal,

        ExperienceLevel experienceLevel,

        @Min(value = 1, message = "Daily learning hours must be at least 1")
        @Max(value = 24, message = "Daily learning hours cannot exceed 24")
        Integer dailyLearningHours,

        LearningStyle learningStyle,

        PreferredContentType preferredContentType
) {
}
