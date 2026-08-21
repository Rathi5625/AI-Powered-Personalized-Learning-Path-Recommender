package com.learningpath.entity;

import com.learningpath.entity.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "learning_activities",
        indexes = {
                @Index(name = "idx_activity_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_activity_type", columnList = "activity_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningActivity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "entity_type", length = 50)
    private String entityType; // e.g. "COURSE", "ASSESSMENT", "PROJECT"

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON or descriptive string

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
