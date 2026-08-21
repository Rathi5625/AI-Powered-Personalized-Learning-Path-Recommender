package com.learningpath.dto;

import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;

public record SettingsDto(
        String fullName,
        String email,
        String location,
        String themePreference,
        boolean emailNotifications,
        boolean pushNotifications,
        Integer dailyLearningHours,
        Integer weeklyCommitmentHours,
        String preferredLearningPace,
        String availableDays,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType
) {
}
