package com.learningpath.service;

import com.learningpath.dto.UpdateProfileRequest;
import com.learningpath.dto.UserProfileResponse;
import com.learningpath.dto.UserSkillResponse;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.exception.ResourceNotFoundException;
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
public class ProfileService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<UserSkillResponse> userSkills = userSkillRepository.findByUserId(userId).stream()
                .map(us -> new UserSkillResponse(
                        us.getId(),
                        us.getUser().getId(),
                        us.getSkill().getId(),
                        us.getSkill().getName(),
                        us.getSkill().getCategory(),
                        us.getProficiencyLevel(),
                        us.getConfidence(),
                        us.getSource(),
                        us.isVerified(),
                        us.getLastAssessedDate(),
                        us.getCreatedAt(),
                        us.getUpdatedAt()
                ))
                .toList();

        int completion = user.calculateProfileCompletionPercentage(userSkills.size());

        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role,
                user.isEmailVerified(),
                user.getLocation(),
                user.getEducation(),
                user.getGraduationYear(),
                user.getCurrentGoal(),
                user.getPersonalObjective(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getGithubUrl(),
                user.getLinkedinUrl(),
                user.getPortfolioUrl(),
                user.getTargetCareer(),
                user.getExperienceLevel(),
                user.getDailyLearningHours(),
                user.getWeeklyCommitmentHours(),
                user.getPreferredLearningPace(),
                user.getAvailableDays(),
                user.getLearningStyle(),
                user.getPreferredContentType(),
                completion,
                userSkills,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.location() != null) user.setLocation(request.location().trim());
        if (request.education() != null) user.setEducation(request.education().trim());
        if (request.graduationYear() != null) user.setGraduationYear(request.graduationYear());
        if (request.currentGoal() != null) user.setCurrentGoal(request.currentGoal().trim());
        if (request.personalObjective() != null) user.setPersonalObjective(request.personalObjective().trim());
        if (request.bio() != null) user.setBio(request.bio().trim());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl().trim());
        if (request.githubUrl() != null) user.setGithubUrl(request.githubUrl().trim());
        if (request.linkedinUrl() != null) user.setLinkedinUrl(request.linkedinUrl().trim());
        if (request.portfolioUrl() != null) user.setPortfolioUrl(request.portfolioUrl().trim());
        if (request.targetCareer() != null) user.setTargetCareer(request.targetCareer().trim());
        if (request.experienceLevel() != null) user.setExperienceLevel(request.experienceLevel());
        if (request.dailyLearningHours() != null) user.setDailyLearningHours(request.dailyLearningHours());
        if (request.weeklyCommitmentHours() != null) user.setWeeklyCommitmentHours(request.weeklyCommitmentHours());
        if (request.preferredLearningPace() != null) user.setPreferredLearningPace(request.preferredLearningPace().trim());
        if (request.availableDays() != null) user.setAvailableDays(request.availableDays().trim());
        if (request.learningStyle() != null) user.setLearningStyle(request.learningStyle());
        if (request.preferredContentType() != null) user.setPreferredContentType(request.preferredContentType());

        User savedUser = userRepository.save(user);
        log.info("[ProfileService] Updated profile for userId={}", savedUser.getId());

        return getProfile(userId);
    }
}
