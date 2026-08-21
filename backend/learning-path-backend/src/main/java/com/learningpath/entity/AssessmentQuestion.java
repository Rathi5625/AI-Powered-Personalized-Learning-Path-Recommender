package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "assessment_questions",
        indexes = {
                @Index(name = "idx_assessment_questions_assessment", columnList = "assessment_id"),
                @Index(name = "idx_assessment_questions_difficulty", columnList = "difficulty")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questions_assessment"))
    private Assessment assessment;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 30)
    @Builder.Default
    private CourseDifficulty difficulty = CourseDifficulty.BEGINNER;

    // Calibration signals
    @Column(name = "total_attempts", nullable = false)
    @Builder.Default
    private int totalAttempts = 0;

    @Column(name = "correct_attempts", nullable = false)
    @Builder.Default
    private int correctAttempts = 0;

    @Column(name = "avg_response_time_seconds")
    @Builder.Default
    private double averageResponseTimeSeconds = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "calibrated_difficulty", length = 30)
    private CourseDifficulty calibratedDifficulty;

    public void recordAttempt(boolean correct, int responseTimeSeconds) {
        this.totalAttempts++;
        if (correct) {
            this.correctAttempts++;
        }
        if (this.totalAttempts > 0) {
            this.averageResponseTimeSeconds = ((this.averageResponseTimeSeconds * (this.totalAttempts - 1)) + responseTimeSeconds) / this.totalAttempts;
        }

        // Calibrate difficulty if sufficient sample size (>= 5 attempts)
        if (this.totalAttempts >= 5) {
            double successRate = (double) this.correctAttempts / this.totalAttempts;
            if (successRate > 0.75) {
                this.calibratedDifficulty = CourseDifficulty.BEGINNER;
            } else if (successRate >= 0.40) {
                this.calibratedDifficulty = CourseDifficulty.INTERMEDIATE;
            } else {
                this.calibratedDifficulty = CourseDifficulty.ADVANCED;
            }
        }
    }
}
