package com.learningpath.entity;

import com.learningpath.entity.enums.ProficiencyLevel;
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
        name = "skill_prerequisites",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_skill_prerequisite", columnNames = {"skill_id", "prerequisite_skill_id"})
        },
        indexes = {
                @Index(name = "idx_skill_prereq_target", columnList = "skill_id"),
                @Index(name = "idx_skill_prereq_prereq", columnList = "prerequisite_skill_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillPrerequisite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_skill_prereq_target_skill"))
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_skill_prereq_prereq_skill"))
    private Skill prerequisiteSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_proficiency", nullable = false, length = 30)
    private ProficiencyLevel requiredProficiency;
}
