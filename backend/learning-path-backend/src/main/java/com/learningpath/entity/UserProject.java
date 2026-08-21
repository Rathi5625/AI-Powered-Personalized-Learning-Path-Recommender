package com.learningpath.entity;

import com.learningpath.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_project", columnNames = {"user_id", "project_id"})
        },
        indexes = {
                @Index(name = "idx_user_projects_user_id", columnList = "user_id"),
                @Index(name = "idx_user_projects_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProject extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status = ProjectStatus.NOT_STARTED;

    @Builder.Default
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(name = "completed_milestones")
    private Integer completedMilestones;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "submission_url", length = 500)
    private String submissionUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
