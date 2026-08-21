package com.learningpath.service;

import com.learningpath.dto.SettingsDto;
import com.learningpath.dto.UpdateSettingsRequest;
import com.learningpath.entity.User;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SettingsDto getSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new SettingsDto(
                user.getFullName(),
                user.getEmail(),
                user.getLocation(),
                user.getThemePreference() != null ? user.getThemePreference() : "system",
                user.isEmailNotifications(),
                user.isPushNotifications(),
                user.getDailyLearningHours(),
                user.getWeeklyCommitmentHours(),
                user.getPreferredLearningPace(),
                user.getAvailableDays(),
                user.getLearningStyle(),
                user.getPreferredContentType()
        );
    }

    @Transactional
    public SettingsDto updateSettings(UUID userId, UpdateSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.location() != null) user.setLocation(request.location().trim());
        if (request.themePreference() != null) user.setThemePreference(request.themePreference().trim());
        if (request.emailNotifications() != null) user.setEmailNotifications(request.emailNotifications());
        if (request.pushNotifications() != null) user.setPushNotifications(request.pushNotifications());
        if (request.dailyLearningHours() != null) user.setDailyLearningHours(request.dailyLearningHours());
        if (request.weeklyCommitmentHours() != null) user.setWeeklyCommitmentHours(request.weeklyCommitmentHours());
        if (request.preferredLearningPace() != null) user.setPreferredLearningPace(request.preferredLearningPace().trim());
        if (request.availableDays() != null) user.setAvailableDays(request.availableDays().trim());
        if (request.learningStyle() != null) user.setLearningStyle(request.learningStyle());
        if (request.preferredContentType() != null) user.setPreferredContentType(request.preferredContentType());

        User saved = userRepository.save(user);
        log.info("[SettingsService] Updated settings for userId={}", saved.getId());

        return getSettings(userId);
    }
}
