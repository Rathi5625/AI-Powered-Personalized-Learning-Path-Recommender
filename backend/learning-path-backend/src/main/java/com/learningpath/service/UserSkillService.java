package com.learningpath.service;

import com.learningpath.dto.UserSkillRequest;
import com.learningpath.dto.UserSkillResponse;
import com.learningpath.dto.UserSkillUpdateRequest;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.SkillSource;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public UserSkillResponse addUserSkill(UUID userId, UserSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.skillId()));

        if (userSkillRepository.existsByUserIdAndSkillId(userId, request.skillId())) {
            throw new DuplicateResourceException("User already has skill '" + skill.getName() + "' assigned");
        }

        SkillSource source = request.source() != null ? request.source() : SkillSource.SELF_REPORTED;

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .proficiencyLevel(request.proficiencyLevel())
                .confidence(request.confidence())
                .source(source)
                .isVerified(source == SkillSource.ASSESSMENT || source == SkillSource.COMPLETED_COURSE)
                .lastAssessedDate(Instant.now())
                .build();

        UserSkill savedUserSkill = userSkillRepository.save(userSkill);
        return mapToUserSkillResponse(savedUserSkill);
    }

    @Transactional(readOnly = true)
    public List<UserSkillResponse> getUserSkills(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return userSkillRepository.findByUserId(userId)
                .stream()
                .map(this::mapToUserSkillResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserSkillResponse getUserSkill(UUID userId, UUID skillId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found for user " + userId));

        return mapToUserSkillResponse(userSkill);
    }

    public UserSkillResponse updateUserSkill(UUID userId, UUID skillId, UserSkillUpdateRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found for user " + userId));

        userSkill.setProficiencyLevel(request.proficiencyLevel());
        if (request.confidence() != null) {
            userSkill.setConfidence(request.confidence());
        }
        if (request.source() != null) {
            userSkill.setSource(request.source());
            if (request.source() == SkillSource.ASSESSMENT || request.source() == SkillSource.COMPLETED_COURSE) {
                userSkill.setVerified(true);
            }
        }
        userSkill.setLastAssessedDate(Instant.now());

        UserSkill updatedUserSkill = userSkillRepository.save(userSkill);
        return mapToUserSkillResponse(updatedUserSkill);
    }

    public void removeUserSkill(UUID userId, UUID skillId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found for user " + userId));

        userSkillRepository.delete(userSkill);
    }

    private UserSkillResponse mapToUserSkillResponse(UserSkill userSkill) {
        return new UserSkillResponse(
                userSkill.getId(),
                userSkill.getUser().getId(),
                userSkill.getSkill().getId(),
                userSkill.getSkill().getName(),
                userSkill.getSkill().getCategory(),
                userSkill.getProficiencyLevel(),
                userSkill.getConfidence(),
                userSkill.getSource(),
                userSkill.isVerified(),
                userSkill.getLastAssessedDate(),
                userSkill.getCreatedAt(),
                userSkill.getUpdatedAt()
        );
    }
}
