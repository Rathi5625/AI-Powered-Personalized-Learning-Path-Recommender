package com.learningpath.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "learning_path_versions",
        indexes = {
                @Index(name = "idx_path_versions_path", columnList = "learning_path_id"),
                @Index(name = "idx_path_versions_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_path_id", nullable = false, foreignKey = @ForeignKey(name = "fk_path_versions_path"))
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_path_versions_user"))
    private User user;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "change_reason", nullable = false, length = 255)
    private String changeReason;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "snapshot_json", columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(name = "overall_progress")
    private Double overallProgress;
}
