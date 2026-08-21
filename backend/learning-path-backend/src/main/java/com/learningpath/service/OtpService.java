package com.learningpath.service;

import com.learningpath.entity.OtpVerification;
import com.learningpath.entity.enums.OtpPurpose;
import com.learningpath.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private int expiryMinutes;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        String normalizedEmail = email.trim().toLowerCase();

        // Check resend cooldown on the most recent OTP
        Optional<OtpVerification> recentOtpOpt = otpVerificationRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose);

        if (recentOtpOpt.isPresent()) {
            OtpVerification recentOtp = recentOtpOpt.get();
            if (recentOtp.getCreatedAt() != null) {
                long elapsedSeconds = Duration.between(recentOtp.getCreatedAt(), Instant.now()).getSeconds();
                if (elapsedSeconds < resendCooldownSeconds) {
                    long remaining = resendCooldownSeconds - elapsedSeconds;
                    throw new IllegalArgumentException(
                            "Please wait " + remaining + " seconds before requesting a new code"
                    );
                }
            }

            // Invalidate existing active unused OTPs
            List<OtpVerification> activeOtps = otpVerificationRepository
                    .findAllByEmailAndPurposeAndUsedFalse(normalizedEmail, purpose);
            for (OtpVerification active : activeOtps) {
                active.setUsed(true);
            }
            otpVerificationRepository.saveAll(activeOtps);
        }

        // Generate cryptographically secure 6-digit OTP
        int randomCode = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(randomCode);

        // Store hashed OTP
        String hashedOtp = passwordEncoder.encode(rawOtp);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expiryMinutes));

        OtpVerification otpVerification = OtpVerification.builder()
                .email(normalizedEmail)
                .otpHash(hashedOtp)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .attemptCount(0)
                .maxAttempts(maxAttempts)
                .used(false)
                .build();

        otpVerificationRepository.save(otpVerification);
        log.info("[OtpService] Generated new OTP for email={}, purpose={}", normalizedEmail, purpose);

        // Send OTP via email
        emailService.sendOtpEmail(normalizedEmail, rawOtp, purpose);
    }

    @Transactional
    public void verifyOtp(String email, String rawOtp, OtpPurpose purpose) {
        String normalizedEmail = email.trim().toLowerCase();

        OtpVerification otpVerification = otpVerificationRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose)
                .orElseThrow(() -> new IllegalArgumentException("No active verification code found. Please request a new code."));

        if (otpVerification.isUsed()) {
            throw new IllegalArgumentException("Verification code has already been used. Please request a new code.");
        }

        if (otpVerification.isExpired()) {
            otpVerification.setUsed(true);
            otpVerificationRepository.save(otpVerification);
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        if (otpVerification.isMaxAttemptsReached()) {
            otpVerification.setUsed(true);
            otpVerificationRepository.save(otpVerification);
            throw new IllegalArgumentException("Too many incorrect attempts. Verification code invalidated. Please request a new code.");
        }

        // Increment attempt count
        otpVerification.setAttemptCount(otpVerification.getAttemptCount() + 1);

        if (!passwordEncoder.matches(rawOtp, otpVerification.getOtpHash())) {
            otpVerificationRepository.save(otpVerification);
            int remaining = otpVerification.getMaxAttempts() - otpVerification.getAttemptCount();
            if (remaining > 0) {
                throw new IllegalArgumentException("Invalid verification code. " + remaining + " attempts remaining.");
            } else {
                otpVerification.setUsed(true);
                otpVerificationRepository.save(otpVerification);
                throw new IllegalArgumentException("Invalid verification code. Maximum attempts reached. Please request a new code.");
            }
        }

        // Mark as successfully verified
        otpVerification.setUsed(true);
        otpVerification.setVerifiedAt(Instant.now());
        otpVerificationRepository.save(otpVerification);
        log.info("[OtpService] Successfully verified OTP for email={}, purpose={}", normalizedEmail, purpose);
    }
}
