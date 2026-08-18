package com.learningpath.entity;

import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @PrePersist
    public void ensureRole() {
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }
}
