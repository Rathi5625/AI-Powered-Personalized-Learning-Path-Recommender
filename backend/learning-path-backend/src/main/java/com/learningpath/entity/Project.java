package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_projects_difficulty", columnList = "difficulty")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "technologies", length = 255)
    private String technologies; // e.g. "React, Node.js, PostgreSQL"

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CourseDifficulty difficulty;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "milestones_count")
    private Integer milestonesCount;

    @Column(name = "repository_template_url", length = 500)
    private String repositoryTemplateUrl;
}
