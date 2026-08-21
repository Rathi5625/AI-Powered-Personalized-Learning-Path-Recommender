package com.learningpath.entity;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    @Column(name = "target_career", length = 100)
    private String targetCareer;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 30)
    private ExperienceLevel experienceLevel;

    @Column(name = "daily_learning_hours")
    private Integer dailyLearningHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style", length = 30)
    private LearningStyle learningStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_content_type", length = 30)
    private PreferredContentType preferredContentType;

    @Builder.Default
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    // Profile & Bio Info
    @Column(length = 150)
    private String location;

    @Column(length = 150)
    private String education;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "current_goal", length = 200)
    private String currentGoal;

    @Column(name = "personal_objective", columnDefinition = "TEXT")
    private String personalObjective;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // Social Links
    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "portfolio_url", length = 255)
    private String portfolioUrl;

    // Preferences
    @Column(name = "weekly_commitment_hours")
    private Integer weeklyCommitmentHours;

    @Column(name = "preferred_learning_pace", length = 50)
    private String preferredLearningPace;

    @Column(name = "available_days", length = 100)
    private String availableDays;

    @Builder.Default
    @Column(name = "theme_preference", length = 30)
    private String themePreference = "system";

    @Builder.Default
    @Column(name = "email_notifications")
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(name = "push_notifications")
    private Boolean pushNotifications = true;

    @Builder.Default
    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted = false;

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    public boolean isOnboardingCompleted() {
        return Boolean.TRUE.equals(this.onboardingCompleted) || (this.targetCareer != null && !this.targetCareer.isBlank() && this.experienceLevel != null);
    }

    public boolean isEmailNotifications() {
        return this.emailNotifications == null || this.emailNotifications;
    }

    public boolean isPushNotifications() {
        return this.pushNotifications == null || this.pushNotifications;
    }

    @PrePersist
    public void ensureRole() {
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }

    public int calculateProfileCompletionPercentage(int skillCount) {
        int totalWeight = 0;
        int earnedWeight = 0;

        // 1. Basic details (25%)
        totalWeight += 25;
        if (fullName != null && !fullName.isBlank() && email != null && !email.isBlank()) {
            earnedWeight += 25;
        }

        // 2. Career & Goal (25%)
        totalWeight += 25;
        int careerPoints = 0;
        if (targetCareer != null && !targetCareer.isBlank()) careerPoints += 10;
        if (experienceLevel != null) careerPoints += 10;
        if (currentGoal != null && !currentGoal.isBlank()) careerPoints += 5;
        earnedWeight += careerPoints;

        // 3. Education & Location (20%)
        totalWeight += 20;
        int eduPoints = 0;
        if (education != null && !education.isBlank()) eduPoints += 10;
        if (location != null && !location.isBlank()) eduPoints += 5;
        if (bio != null && !bio.isBlank()) eduPoints += 5;
        earnedWeight += eduPoints;

        // 4. Learning Preferences (15%)
        totalWeight += 15;
        int prefPoints = 0;
        if (dailyLearningHours != null || weeklyCommitmentHours != null) prefPoints += 5;
        if (learningStyle != null) prefPoints += 5;
        if (preferredContentType != null || preferredLearningPace != null) prefPoints += 5;
        earnedWeight += prefPoints;

        // 5. Skills (15%)
        totalWeight += 15;
        if (skillCount >= 3) {
            earnedWeight += 15;
        } else if (skillCount > 0) {
            earnedWeight += (skillCount * 5);
        }

        return Math.min(100, Math.max(10, (int) Math.round(((double) earnedWeight / totalWeight) * 100)));
    }
}
