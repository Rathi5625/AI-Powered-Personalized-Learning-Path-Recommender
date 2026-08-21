package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "adaptive_assessment_responses",
        indexes = {
                @Index(name = "idx_adaptive_resp_session", columnList = "session_id"),
                @Index(name = "idx_adaptive_resp_question", columnList = "question_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdaptiveAssessmentResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_adaptive_resp_session"))
    private AdaptiveAssessmentSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_adaptive_resp_question"))
    private AssessmentQuestion question;

    @Column(name = "concept_name", nullable = false, length = 100)
    private String conceptName;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 30)
    private CourseDifficulty difficulty;

    @Column(name = "selected_answer", nullable = false, columnDefinition = "TEXT")
    private String selectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "response_time_seconds", nullable = false)
    private int responseTimeSeconds;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "bkt_prob_before", nullable = false)
    private double bktProbabilityBefore;

    @Column(name = "bkt_prob_after", nullable = false)
    private double bktProbabilityAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_before", length = 30)
    private CourseDifficulty difficultyBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_after", length = 30)
    private CourseDifficulty difficultyAfter;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "possible_guess", nullable = false)
    @Builder.Default
    private boolean possibleGuess = false;

    @Column(name = "possible_careless_error", nullable = false)
    @Builder.Default
    private boolean possibleCarelessError = false;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private Instant answeredAt = Instant.now();
}
