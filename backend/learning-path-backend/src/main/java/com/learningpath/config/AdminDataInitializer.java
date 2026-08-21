package com.learningpath.config;

import com.learningpath.entity.User;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:${admin.email:admin@learnai.local}}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:${admin.password:ChangeThisAdminPassword123!}}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("[AdminDataInitializer] Initializing development admin verification for email={}", adminEmail);

        Optional<User> existingUserOpt = userRepository.findByEmail(adminEmail);
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            boolean updated = false;

            if (existingUser.getRole() != UserRole.ADMIN) {
                log.info("[AdminDataInitializer] Upgrading user '{}' to ADMIN role", adminEmail);
                existingUser.setRole(UserRole.ADMIN);
                updated = true;
            }
            if (!existingUser.isEmailVerified()) {
                existingUser.setEmailVerified(true);
                updated = true;
            }

            if (updated) {
                userRepository.save(existingUser);
            }
            log.info("[AdminDataInitializer] Admin account verified: email={}, role={}", adminEmail, existingUser.getRole());
        } else {
            log.info("[AdminDataInitializer] Seeding development admin account: email={}", adminEmail);
            String encodedPassword = passwordEncoder.encode(adminPassword);

            User admin = User.builder()
                    .fullName("LearnAI Administrator")
                    .email(adminEmail)
                    .passwordHash(encodedPassword)
                    .role(UserRole.ADMIN)
                    .targetCareer("Platform Architecture & System Administration")
                    .experienceLevel(ExperienceLevel.ADVANCED)
                    .dailyLearningHours(4)
                    .emailVerified(true)
                    .build();

            User savedAdmin = userRepository.save(admin);
            log.info("[AdminDataInitializer] Development admin account created successfully with id={}", savedAdmin.getId());
        }
    }
}
