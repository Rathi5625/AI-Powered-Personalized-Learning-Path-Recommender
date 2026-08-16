package com.learningpath.entity;

import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
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

@Entity
@Table(
        name = "course_skills",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_course_skill", columnNames = {"course_id", "skill_id"})
        },
        indexes = {
                @Index(name = "idx_course_skills_course", columnList = "course_id"),
                @Index(name = "idx_course_skills_skill", columnList = "skill_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_course_skills_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_course_skills_skill"))
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_level", length = 30)
    @Builder.Default
    private CoverageLevel coverageLevel = CoverageLevel.INTERMEDIATE;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private SkillPriority importance = SkillPriority.HIGH;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_proficiency", nullable = false, length = 30)
    @Builder.Default
    private ProficiencyLevel targetProficiency = ProficiencyLevel.INTERMEDIATE;

    @Column(name = "is_primary_skill", nullable = false)
    @Builder.Default
    private boolean isPrimarySkill = false;
}
