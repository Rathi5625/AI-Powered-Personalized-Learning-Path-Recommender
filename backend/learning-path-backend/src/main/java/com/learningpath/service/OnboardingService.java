package com.learningpath.service;

import com.learningpath.dto.OnboardingCompleteRequest;
import com.learningpath.dto.UserProfileResponse;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.ActivityType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final ProfileService profileService;
    private final LearningActivityService activityService;

    @Transactional
    public UserProfileResponse completeOnboarding(UUID userId, OnboardingCompleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.targetCareer() != null) user.setTargetCareer(request.targetCareer().trim());
        if (request.experienceLevel() != null) user.setExperienceLevel(request.experienceLevel());
        if (request.learningStyle() != null) user.setLearningStyle(request.learningStyle());
        if (request.preferredContentType() != null) user.setPreferredContentType(request.preferredContentType());
        if (request.preferredLearningPace() != null) user.setPreferredLearningPace(request.preferredLearningPace().trim());
        if (request.weeklyCommitmentHours() != null) user.setWeeklyCommitmentHours(request.weeklyCommitmentHours());
        if (request.availableDays() != null) user.setAvailableDays(request.availableDays().trim());
        if (request.currentGoal() != null) user.setCurrentGoal(request.currentGoal().trim());
        if (request.personalObjective() != null) user.setPersonalObjective(request.personalObjective().trim());
        user.setOnboardingCompleted(true);

        userRepository.save(user);

        // Process selected skills
        if (request.selectedSkills() != null && !request.selectedSkills().isEmpty()) {
            for (String skillName : request.selectedSkills()) {
                if (skillName == null || skillName.isBlank()) continue;
                String normalizedName = skillName.trim();

                Skill skill = skillRepository.findByNameIgnoreCase(normalizedName)
                        .orElseGet(() -> skillRepository.save(
                                Skill.builder()
                                        .name(normalizedName)
                                        .category("General")
                                        .build()
                        ));

                if (!userSkillRepository.existsByUserIdAndSkillId(userId, skill.getId())) {
                    UserSkill userSkill = UserSkill.builder()
                            .user(user)
                            .skill(skill)
                            .proficiencyLevel(
                                    request.experienceLevel() != null
                                            ? ProficiencyLevel.valueOf(request.experienceLevel().name())
                                            : ProficiencyLevel.BEGINNER
                            )
                            .isVerified(false)
                            .build();

                    userSkillRepository.save(userSkill);
                }
            }
        }

        // Telemetry event for future ML
        activityService.logActivity(
                userId,
                ActivityType.PROFILE_UPDATE,
                "ONBOARDING",
                user.getId().toString(),
                "career=" + user.getTargetCareer() + ",experience=" + user.getExperienceLevel(),
                null
        );

        log.info("[OnboardingService] Onboarding completed successfully for userId={}", userId);

        return profileService.getProfile(userId);
    }
}
