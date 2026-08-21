package com.learningpath.entity;

import com.learningpath.entity.enums.MasteryLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "learner_knowledge_states",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_skill_concept", columnNames = {"user_id", "concept_name"})
        },
        indexes = {
                @Index(name = "idx_knowledge_user", columnList = "user_id"),
                @Index(name = "idx_knowledge_concept", columnList = "concept_name"),
                @Index(name = "idx_knowledge_mastery", columnList = "mastery_level"),
                @Index(name = "idx_knowledge_revision", columnList = "revision_required")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerKnowledgeState extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "concept_name", nullable = false, length = 150)
    private String conceptName;

    @Column(name = "knowledge_probability", nullable = false)
    @Builder.Default
    private double knowledgeProbability = 0.20;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "correct_attempts", nullable = false)
    @Builder.Default
    private int correctAttempts = 0;

    @Column(name = "incorrect_attempts", nullable = false)
    @Builder.Default
    private int incorrectAttempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 30)
    @Builder.Default
    private MasteryLevel masteryLevel = MasteryLevel.NOT_STARTED;

    @Column(name = "confidence_score", nullable = false)
    @Builder.Default
    private double confidenceScore = 0.50;

    @Column(name = "consecutive_correct", nullable = false)
    @Builder.Default
    private int consecutiveCorrect = 0;

    @Column(name = "consecutive_incorrect", nullable = false)
    @Builder.Default
    private int consecutiveIncorrect = 0;

    @Column(name = "avg_response_time_seconds")
    @Builder.Default
    private double averageResponseTimeSeconds = 0.0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_correct_at")
    private Instant lastCorrectAt;

    @Column(name = "revision_required", nullable = false)
    @Builder.Default
    private boolean revisionRequired = false;
}
