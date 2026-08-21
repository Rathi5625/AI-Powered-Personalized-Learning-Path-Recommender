package com.learningpath.dto;

import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;

public record UpdateSettingsRequest(
        String fullName,
        String location,
        String themePreference,
        Boolean emailNotifications,
        Boolean pushNotifications,
        Integer dailyLearningHours,
        Integer weeklyCommitmentHours,
        String preferredLearningPace,
        String availableDays,
        LearningStyle learningStyle,
        PreferredContentType preferredContentType
) {
}
