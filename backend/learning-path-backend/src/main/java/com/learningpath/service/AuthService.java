package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.User;
import com.learningpath.exception.EmailAlreadyExistsException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("[AuthService] Processing signup request for email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("User with email '" + request.email() + "' already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.name())
                .email(request.email())
                .passwordHash(hashedPassword)
                .targetCareer(request.targetCareer())
                .experienceLevel(request.experienceLevel())
                .dailyLearningHours(request.dailyLearningHours())
                .learningStyle(request.learningStyle())
                .preferredContentType(request.preferredContentType())
                .build();

        User savedUser = userRepository.save(user);
        log.info("[AuthService] User registered successfully with id={}", savedUser.getId());

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                "Account created successfully"
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("[AuthService] Processing login request for email={}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("[AuthService] Password mismatch for email={}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return new AuthResponse(
                token,
                "Bearer",
                expiresInSeconds,
                new UserSummaryResponse(user.getId(), user.getFullName(), user.getEmail())
        );
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new AuthenticatedUserResponse(
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
