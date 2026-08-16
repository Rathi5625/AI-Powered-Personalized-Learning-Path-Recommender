package com.learningpath.entity;

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
        name = "career_skills",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_career_skill", columnNames = {"career_id", "skill_id"})
        },
        indexes = {
                @Index(name = "idx_career_skills_career", columnList = "career_id"),
                @Index(name = "idx_career_skills_skill", columnList = "skill_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_id", nullable = false, foreignKey = @ForeignKey(name = "fk_career_skills_career"))
    private Career career;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_career_skills_skill"))
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private SkillPriority priority = SkillPriority.HIGH;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_proficiency", nullable = false, length = 30)
    private ProficiencyLevel requiredProficiency;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private boolean isMandatory = true;
}
