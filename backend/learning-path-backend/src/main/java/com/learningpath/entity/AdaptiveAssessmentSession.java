package com.learningpath.entity;

import com.learningpath.entity.enums.AdaptiveSessionStatus;
import com.learningpath.entity.enums.ConfidenceLevel;
import com.learningpath.entity.enums.CourseDifficulty;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "adaptive_assessment_sessions",
        indexes = {
                @Index(name = "idx_adaptive_session_user", columnList = "user_id"),
                @Index(name = "idx_adaptive_session_assessment", columnList = "assessment_id"),
                @Index(name = "idx_adaptive_session_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdaptiveAssessmentSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_adaptive_session_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_adaptive_session_assessment"))
    private Assessment assessment;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AdaptiveSessionStatus status = AdaptiveSessionStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_difficulty", nullable = false, length = 30)
    @Builder.Default
    private CourseDifficulty currentDifficulty = CourseDifficulty.BEGINNER;

    @Column(name = "questions_asked", nullable = false)
    @Builder.Default
    private int questionsAsked = 0;

    @Column(name = "correct_answers", nullable = false)
    @Builder.Default
    private int correctAnswers = 0;

    @Column(name = "incorrect_answers", nullable = false)
    @Builder.Default
    private int incorrectAnswers = 0;

    @Column(name = "avg_response_time_seconds")
    @Builder.Default
    private double averageResponseTimeSeconds = 0.0;

    @Column(name = "current_ability_estimate")
    @Builder.Default
    private double currentAbilityEstimate = 0.50;

    @Column(name = "confidence_score")
    @Builder.Default
    private double confidenceScore = 0.50;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level", length = 30)
    @Builder.Default
    private ConfidenceLevel confidenceLevel = ConfidenceLevel.LOW;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    public void recordAnswer(boolean correct, int responseTimeSeconds, CourseDifficulty nextDifficulty, double nextAbility, double nextConfidence) {
        this.questionsAsked++;
        if (correct) {
            this.correctAnswers++;
        } else {
            this.incorrectAnswers++;
        }

        if (this.questionsAsked > 0) {
            this.averageResponseTimeSeconds = ((this.averageResponseTimeSeconds * (this.questionsAsked - 1)) + responseTimeSeconds) / this.questionsAsked;
        }

        this.currentDifficulty = nextDifficulty != null ? nextDifficulty : this.currentDifficulty;
        this.currentAbilityEstimate = Math.max(0.01, Math.min(0.99, nextAbility));
        this.confidenceScore = Math.max(0.01, Math.min(1.0, nextConfidence));

        if (this.confidenceScore >= 0.80) {
            this.confidenceLevel = ConfidenceLevel.HIGH;
        } else if (this.confidenceScore >= 0.55) {
            this.confidenceLevel = ConfidenceLevel.MEDIUM;
        } else {
            this.confidenceLevel = ConfidenceLevel.LOW;
        }
    }
}
