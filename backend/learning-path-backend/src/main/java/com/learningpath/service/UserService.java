package com.learningpath.service;

import com.learningpath.dto.UserCreateRequest;
import com.learningpath.dto.UserResponse;
import com.learningpath.dto.UserUpdateRequest;
import com.learningpath.entity.User;
import com.learningpath.exception.EmailAlreadyExistsException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("User with email '" + request.email() + "' already exists");
        }

        User user = User.builder()
                .email(request.email())
                .fullName(request.name())
                .targetCareer(request.careerGoal())
                .experienceLevel(request.experienceLevel())
                .dailyLearningHours(request.dailyLearningHours())
                .learningStyle(request.learningStyle())
                .preferredContentType(request.preferredContentType())
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setFullName(request.name());
        user.setTargetCareer(request.careerGoal());
        user.setExperienceLevel(request.experienceLevel());
        user.setDailyLearningHours(request.dailyLearningHours());
        user.setLearningStyle(request.learningStyle());
        user.setPreferredContentType(request.preferredContentType());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getTargetCareer(),
                user.getExperienceLevel(),
                user.getDailyLearningHours(),
                user.getLearningStyle(),
                user.getPreferredContentType(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
