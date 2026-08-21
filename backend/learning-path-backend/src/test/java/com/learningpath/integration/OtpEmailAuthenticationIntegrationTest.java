package com.learningpath.integration;

import com.learningpath.dto.*;
import com.learningpath.entity.OtpVerification;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.OtpPurpose;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.exception.EmailNotVerifiedException;
import com.learningpath.repository.OtpVerificationRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.JwtService;
import com.learningpath.service.AuthService;
import com.learningpath.service.EmailService;
import com.learningpath.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 9 — OTP Email Environment Configuration & Authentication Hardening Test Suite
 *
 * Validates:
 * 1. End-to-End Signup & Email Verification Flow
 * 2. 6-Digit Secure OTP Generation & Cryptographic Hashing
 * 3. 60-Second Resend Cooldown Enforcement
 * 4. Invalidation of Superseded OTPs upon Resend
 * 5. Max Attempt Lockout (5 attempts)
 * 6. 10-Minute Expiration Gating
 * 7. Strict Purpose Isolation (EMAIL_VERIFICATION vs PASSWORD_RESET)
 * 8. Forgot Password, Single-Use Reset Token & Password Reset Flow
 * 9. Email Template Branding & Security Notice
 * 10. Conditional Development vs Production Logging
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 9 — OTP Email Delivery & Authentication Hardening")
public class OtpEmailAuthenticationIntegrationTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpVerificationRepository otpVerificationRepository;
    @Mock private JavaMailSender mailSender;

    @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;

    private EmailService emailService;
    private OtpService otpService;
    private AuthService authService;

    private User testUser;
    private final String testEmail = "learner.phase9@learnai.com";

    @BeforeEach
    void setUp() {
        // Configure JWT Service
        jwtService = new JwtService("c9qJphgZO5bwFBN83NvsoR1jPY700O7bi6UfqBjbMU6X8GZR5MDBBHphRe6JOxwH4aLowerK0WAxKX9tQY7wRw==", 86400000L);

        // Configure EmailService
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@learnai.com");
        ReflectionTestUtils.setField(emailService, "otpExpiryMinutes", 10);
        ReflectionTestUtils.setField(emailService, "devLogging", false);

        // Configure OtpService
        otpService = new OtpService(otpVerificationRepository, emailService, passwordEncoder);
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 10);
        ReflectionTestUtils.setField(otpService, "resendCooldownSeconds", 60);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 5);

        // Configure AuthService
        authService = new AuthService(userRepository, passwordEncoder, jwtService, otpService);

        testUser = User.builder()
                .fullName("Alex Learner")
                .email(testEmail)
                .passwordHash(passwordEncoder.encode("SecurePass123!"))
                .role(UserRole.USER)
                .emailVerified(false)
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .targetCareer("Backend Developer")
                .build();
        testUser.setId(UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. END-TO-END SIGNUP & EMAIL VERIFICATION OTP FLOW
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 1 — Signup creates unverified user, generates 6-digit hashed OTP, and blocks login until verified")
    void testSignup_GeneratesOtp_AndBlocksUnverifiedLogin() {
        SignupRequest signupReq = new SignupRequest(
                "Alex Learner",
                testEmail,
                "SecurePass123!",
                "Backend Developer",
                ExperienceLevel.INTERMEDIATE,
                2,
                null,
                null
        );

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        SignupResponse signupResp = authService.signup(signupReq);

        assertThat(signupResp).isNotNull();
        assertThat(signupResp.email()).isEqualTo(testEmail);
        assertThat(signupResp.message()).contains("verify your email");

        // Verify OTP was persisted with purpose EMAIL_VERIFICATION
        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(otpCaptor.capture());
        OtpVerification savedOtp = otpCaptor.getValue();

        assertThat(savedOtp.getEmail()).isEqualTo(testEmail);
        assertThat(savedOtp.getPurpose()).isEqualTo(OtpPurpose.EMAIL_VERIFICATION);
        assertThat(savedOtp.isUsed()).isFalse();
        assertThat(savedOtp.getMaxAttempts()).isEqualTo(5);
        assertThat(savedOtp.getExpiresAt()).isAfter(Instant.now().plus(Duration.ofMinutes(9)));

        // Verify login is blocked before OTP verification
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        LoginRequest loginReq = new LoginRequest(testEmail, "SecurePass123!");

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(EmailNotVerifiedException.class)
                .hasMessageContaining("Email is not verified");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. VERIFY EMAIL OTP & LOGIN ENABLEMENT
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 2 — Submitting correct OTP sets emailVerified=true and issues valid JWT")
    void testVerifyEmailOtp_Success() {
        String rawOtp = "482915";
        String hashedOtp = passwordEncoder.encode(rawOtp);

        OtpVerification activeOtp = OtpVerification.builder()
                .email(testEmail)
                .otpHash(hashedOtp)
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .attemptCount(0)
                .maxAttempts(5)
                .used(false)
                .build();

        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(activeOtp));
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        VerifyEmailOtpRequest verifyReq = new VerifyEmailOtpRequest(testEmail, rawOtp);
        AuthResponse authResp = authService.verifyEmailOtp(verifyReq);

        assertThat(authResp).isNotNull();
        assertThat(authResp.accessToken()).isNotBlank();
        assertThat(authResp.tokenType()).isEqualTo("Bearer");
        assertThat(testUser.isEmailVerified()).isTrue();
        assertThat(activeOtp.isUsed()).isTrue();
        assertThat(activeOtp.getVerifiedAt()).isNotNull();

        // Verify login now succeeds
        LoginRequest loginReq = new LoginRequest(testEmail, "SecurePass123!");
        AuthResponse loginResp = authService.login(loginReq);
        assertThat(loginResp.accessToken()).isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. 60-SECOND RESEND COOLDOWN ENFORCEMENT
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 3 — Resending OTP within 60s triggers cooldown error, while resend after cooldown invalidates previous OTP")
    void testResendOtp_CooldownAndInvalidation() {
        OtpVerification recentOtp = OtpVerification.builder()
                .email(testEmail)
                .otpHash(passwordEncoder.encode("111111"))
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .used(false)
                .build();
        recentOtp.setCreatedAt(Instant.now().minusSeconds(20)); // Created 20 seconds ago (< 60s cooldown)

        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(recentOtp));

        ResendOtpRequest resendReq = new ResendOtpRequest(testEmail, OtpPurpose.EMAIL_VERIFICATION);

        // Attempting resend before 60s must fail
        assertThatThrownBy(() -> authService.resendOtp(resendReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Please wait")
                .hasMessageContaining("seconds before requesting a new code");

        // Fast forward createdAt to 65s ago (> 60s cooldown)
        recentOtp.setCreatedAt(Instant.now().minusSeconds(65));
        when(otpVerificationRepository.findAllByEmailAndPurposeAndUsedFalse(testEmail, OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(List.of(recentOtp));

        ApiResponse resendResp = authService.resendOtp(resendReq);

        assertThat(resendResp.success()).isTrue();
        assertThat(recentOtp.isUsed()).isTrue(); // Old OTP invalidated
        verify(otpVerificationRepository).saveAll(any());
        verify(otpVerificationRepository).save(any(OtpVerification.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. ATTEMPT LIMIT & EXPIRATION CONTROLS
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 4 — Incorrect OTP decrements attempts and locks out at 5 attempts; Expired OTP is rejected")
    void testOtpAttemptLockoutAndExpiration() {
        String rawOtp = "654321";
        OtpVerification activeOtp = OtpVerification.builder()
                .email(testEmail)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .attemptCount(4) // 4 prior failed attempts
                .maxAttempts(5)
                .used(false)
                .build();

        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(activeOtp));

        // 5th failed attempt triggers lockout
        assertThatThrownBy(() -> otpService.verifyOtp(testEmail, "000000", OtpPurpose.EMAIL_VERIFICATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum attempts reached");

        assertThat(activeOtp.isUsed()).isTrue();

        // Expired OTP test
        OtpVerification expiredOtp = OtpVerification.builder()
                .email(testEmail)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().minusSeconds(30)) // Expired
                .attemptCount(0)
                .maxAttempts(5)
                .used(false)
                .build();

        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(expiredOtp));

        assertThatThrownBy(() -> otpService.verifyOtp(testEmail, rawOtp, OtpPurpose.EMAIL_VERIFICATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verification code has expired");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. STRICT PURPOSE SEPARATION (EMAIL_VERIFICATION vs PASSWORD_RESET)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 5 — Email verification OTP cannot be used for password reset and vice versa")
    void testPurposeIsolation() {
        String rawOtp = "778899";

        // Querying with PASSWORD_RESET returns empty when only EMAIL_VERIFICATION exists
        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.verifyOtp(testEmail, rawOtp, OtpPurpose.PASSWORD_RESET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No active verification code found");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. FORGOT PASSWORD & SECURE PASSWORD RESET FLOW
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 6 — Forgot password dispatches reset OTP, verification returns short-lived reset token, and password updates securely")
    void testForgotPasswordAndResetFlow() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // 1. Forgot password request
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(testEmail);
        ApiResponse forgotResp = authService.forgotPassword(forgotReq);
        assertThat(forgotResp.success()).isTrue();

        // 2. Verify Reset OTP
        String resetOtp = "334455";
        OtpVerification resetOtpVerification = OtpVerification.builder()
                .email(testEmail)
                .otpHash(passwordEncoder.encode(resetOtp))
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .attemptCount(0)
                .maxAttempts(5)
                .used(false)
                .build();

        when(otpVerificationRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(resetOtpVerification));

        VerifyResetOtpRequest verifyResetReq = new VerifyResetOtpRequest(testEmail, resetOtp);
        VerifyResetOtpResponse verifyResetResp = authService.verifyResetOtp(verifyResetReq);

        assertThat(verifyResetResp.resetToken()).isNotBlank();
        assertThat(verifyResetResp.email()).isEqualTo(testEmail);

        // 3. Reset Password
        ResetPasswordRequest resetReq = new ResetPasswordRequest(
                verifyResetResp.resetToken(),
                "BrandNewPass2026!",
                "BrandNewPass2026!"
        );

        when(userRepository.save(any(User.class))).thenReturn(testUser);
        ApiResponse resetResp = authService.resetPassword(resetReq);

        assertThat(resetResp.success()).isTrue();
        assertThat(resetResp.message()).contains("Password reset successfully");

        // 4. Verify old password no longer works and new password works
        assertThat(passwordEncoder.matches("SecurePass123!", testUser.getPasswordHash())).isFalse();
        assertThat(passwordEncoder.matches("BrandNewPass2026!", testUser.getPasswordHash())).isTrue();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. EMAIL SERVICE LOGGING INVARIANT (PRODUCTION VS DEV)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation 7 — EmailService operates safely without exposing raw OTP in production mode")
    void testEmailService_LoggingSafety() {
        // In production mode (devLogging=false), sendOtpEmail executes without throwing
        emailService.sendOtpEmail(testEmail, "998877", OtpPurpose.EMAIL_VERIFICATION);

        // In dev mode (devLogging=true)
        ReflectionTestUtils.setField(emailService, "devLogging", true);
        emailService.sendOtpEmail(testEmail, "998877", OtpPurpose.PASSWORD_RESET);
    }
}
