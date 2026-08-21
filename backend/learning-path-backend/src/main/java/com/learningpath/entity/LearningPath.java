package com.learningpath.entity;

import com.learningpath.entity.enums.LearningPathStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "learning_paths",
        indexes = {
                @Index(name = "idx_learning_paths_user", columnList = "user_id"),
                @Index(name = "idx_learning_paths_career", columnList = "target_career_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_learning_paths_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_career_id", foreignKey = @ForeignKey(name = "fk_learning_paths_career"))
    private Career targetCareer;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LearningPathStatus status = LearningPathStatus.ACTIVE;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "overall_progress", nullable = false)
    @Builder.Default
    private Double overallProgress = 0.0;

    @Column(name = "estimated_total_hours")
    @Builder.Default
    private Double estimatedTotalHours = 0.0;

    @Column(name = "completed_hours")
    @Builder.Default
    private Double completedHours = 0.0;

    @Column(name = "current_node_id")
    private UUID currentNodeId;

    @Column(name = "quality_score")
    @Builder.Default
    private Double qualityScore = 90.0;

    @Column(name = "last_recalculated_at")
    private Instant lastRecalculatedAt;

    @Column(name = "recalculation_reason", length = 255)
    private String recalculationReason;
}
