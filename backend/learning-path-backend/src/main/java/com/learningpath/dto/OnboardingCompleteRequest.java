package com.learningpath.dto;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;

import java.util.List;

public record OnboardingCompleteRequest(
        String targetCareer,
        ExperienceLevel experienceLevel,
        List<String> selectedSkills,
        List<String> interests,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType,
        String preferredLearningPace,
        Integer weeklyCommitmentHours,
        String availableDays,
        String currentGoal,
        String personalObjective
) {
}
