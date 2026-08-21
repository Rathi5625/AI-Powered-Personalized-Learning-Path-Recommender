package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathNodeType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "learning_path_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_path_item_order", columnNames = {"learning_path_id", "item_order"})
        },
        indexes = {
                @Index(name = "idx_path_items_path", columnList = "learning_path_id"),
                @Index(name = "idx_path_items_course", columnList = "course_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_path_id", nullable = false, foreignKey = @ForeignKey(name = "fk_path_items_path"))
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", foreignKey = @ForeignKey(name = "fk_path_items_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_skill_id", foreignKey = @ForeignKey(name = "fk_path_items_skill"))
    private Skill targetSkill;

    @Column(name = "title", length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 30)
    @Builder.Default
    private LearningPathNodeType nodeType = LearningPathNodeType.COURSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private LearningPathNodeStatus status = LearningPathNodeStatus.UNLOCKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 30)
    @Builder.Default
    private CourseDifficulty difficulty = CourseDifficulty.BEGINNER;

    @Column(name = "phase_number")
    private Integer phaseNumber;

    @Column(name = "phase_title", length = 150)
    private String phaseTitle;

    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration;

    @Column(name = "estimated_minutes")
    @Builder.Default
    private Integer estimatedMinutes = 45;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "unlock_reason", columnDefinition = "TEXT")
    private String unlockReason;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Column(name = "mastery_requirement")
    @Builder.Default
    private Double masteryRequirement = 0.70;

    @Column(name = "current_mastery")
    @Builder.Default
    private Double currentMastery = 0.0;

    @Column(name = "recommendation_score")
    @Builder.Default
    private Double recommendationScore = 85.0;

    @Column(name = "prerequisite_node_ids", columnDefinition = "TEXT")
    private String prerequisiteNodeIds;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean isCompleted = false;

    @Column(name = "completed_at")
    private Instant completedAt;
}
