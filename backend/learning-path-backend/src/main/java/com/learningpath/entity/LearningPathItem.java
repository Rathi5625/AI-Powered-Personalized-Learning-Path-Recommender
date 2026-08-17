package com.learningpath.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_path_items_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_skill_id", foreignKey = @ForeignKey(name = "fk_path_items_skill"))
    private Skill targetSkill;

    @Column(name = "phase_number")
    private Integer phaseNumber;

    @Column(name = "phase_title", length = 150)
    private String phaseTitle;

    @Column(name = "estimated_duration", length = 50)
    private String estimatedDuration;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean isCompleted = false;
}
