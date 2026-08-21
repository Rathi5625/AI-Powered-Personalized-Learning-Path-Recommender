package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.OtpPurpose;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.exception.EmailAlreadyExistsException;
import com.learningpath.exception.EmailNotVerifiedException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        log.info("[AuthService] Processing signup request for email={}", request.email());

        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("User with email '" + normalizedEmail + "' already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.name().trim())
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .role(UserRole.USER)
                .emailVerified(false)
                .targetCareer(request.targetCareer())
                .experienceLevel(request.experienceLevel())
                .dailyLearningHours(request.dailyLearningHours())
                .learningStyle(request.learningStyle())
                .preferredContentType(request.preferredContentType())
                .build();

        User savedUser = userRepository.save(user);
        log.info("[AuthService] User registered successfully with id={}, emailVerified=false", savedUser.getId());

        // Generate and dispatch email verification OTP
        otpService.generateAndSendOtp(savedUser.getEmail(), OtpPurpose.EMAIL_VERIFICATION);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                "Account created successfully. Please verify your email with the OTP sent to your inbox."
        );
    }

    @Transactional
    public AuthResponse verifyEmailOtp(VerifyEmailOtpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("[AuthService] Verifying email OTP for email={}", normalizedEmail);

        // Verify OTP (throws IllegalArgumentException if invalid/expired)
        otpService.verifyOtp(normalizedEmail, request.otp(), OtpPurpose.EMAIL_VERIFICATION);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + normalizedEmail));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("[AuthService] Email successfully verified for userId={}, email={}", user.getId(), user.getEmail());

        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role.name());
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return new AuthResponse(
                token,
                "Bearer",
                expiresInSeconds,
                new UserSummaryResponse(user.getId(), user.getFullName(), user.getEmail(), role, user.isEmailVerified(), user.isOnboardingCompleted())
        );
    }

    public ApiResponse resendOtp(ResendOtpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("[AuthService] Resending OTP for email={}, purpose={}", normalizedEmail, request.purpose());

        if (request.purpose() == OtpPurpose.PASSWORD_RESET) {
            // For password reset, check if user exists to avoid generating orphan reset OTPs
            if (userRepository.findByEmail(normalizedEmail).isPresent()) {
                otpService.generateAndSendOtp(normalizedEmail, request.purpose());
            }
        } else {
            otpService.generateAndSendOtp(normalizedEmail, request.purpose());
        }

        return new ApiResponse(true, "Verification code sent successfully.");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("[AuthService] Processing login request for email={}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("[AuthService] Password mismatch for email={}", normalizedEmail);
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check email verification status
        if (!user.isEmailVerified()) {
            log.warn("[AuthService] Login blocked: Email not verified for email={}", normalizedEmail);
            throw new EmailNotVerifiedException(
                    "Email is not verified. Please verify your email with the OTP sent to your inbox.",
                    normalizedEmail
            );
        }

        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
        String token = jwtService.generateToken(user.getId(), user.getEmail(), role.name());
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return new AuthResponse(
                token,
                "Bearer",
                expiresInSeconds,
                new UserSummaryResponse(user.getId(), user.getFullName(), user.getEmail(), role, user.isEmailVerified(), user.isOnboardingCompleted())
        );
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("[AuthService] Processing forgot password request for email={}", normalizedEmail);

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent()) {
            try {
                otpService.generateAndSendOtp(normalizedEmail, OtpPurpose.PASSWORD_RESET);
            } catch (Exception ex) {
                log.error("[AuthService] Error dispatching password reset OTP: {}", ex.getMessage());
            }
        } else {
            log.info("[AuthService] Forgot password requested for non-existent email={}. Generic response returned.", normalizedEmail);
        }

        // Always return generic success message to prevent user enumeration
        return new ApiResponse(true, "If an account exists for this email, a verification code has been sent.");
    }

    @Transactional
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("[AuthService] Verifying password reset OTP for email={}", normalizedEmail);

        // Verify reset OTP
        otpService.verifyOtp(normalizedEmail, request.otp(), OtpPurpose.PASSWORD_RESET);

        // Generate short-lived JWT reset token
        String resetToken = jwtService.generatePasswordResetToken(normalizedEmail);

        return new VerifyResetOtpResponse(resetToken, normalizedEmail, "OTP verified successfully.");
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        log.info("[AuthService] Processing reset password with token");

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (request.newPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Validate token and extract email
        String email = jwtService.validatePasswordResetTokenAndGetEmail(request.resetToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("[AuthService] Password reset successfully for userId={}, email={}", user.getId(), user.getEmail());

        return new ApiResponse(true, "Password reset successfully. You can now sign in.");
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;

        return new AuthenticatedUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role,
                user.getTargetCareer(),
                user.getExperienceLevel(),
                user.getDailyLearningHours(),
                user.getLearningStyle(),
                user.getPreferredContentType(),
                user.isEmailVerified(),
                user.isOnboardingCompleted(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
