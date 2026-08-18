package com.learningpath.entity;

import com.learningpath.entity.enums.SkillMappingType;
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

@Entity
@Table(
        name = "skill_aliases",
        indexes = {
                @Index(name = "idx_skill_aliases_dataset_name", columnList = "dataset_skill_name"),
                @Index(name = "idx_skill_aliases_canonical_skill", columnList = "canonical_skill_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAlias extends BaseEntity {

    @Column(name = "dataset_skill_name", nullable = false, unique = true, length = 100)
    private String datasetSkillName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "canonical_skill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_skill_aliases_skill"))
    private Skill canonicalSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false, length = 30)
    private SkillMappingType mappingType;

    @Column(nullable = false)
    private Double confidence;

    @Column(length = 500)
    private String reason;
}
