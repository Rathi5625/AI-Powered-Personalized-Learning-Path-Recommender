package com.learningpath.recommendation.engine;

import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillGapEngineTest {

    private SkillGapEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SkillGapEngine();
    }

    @Test
    void evaluateSkillGapShouldIdentifyFullGap() {
        Skill skill = Skill.builder()
                .name("Spring Boot")
                .category("Backend")
                .build();
        skill.setId(UUID.randomUUID());

        CareerSkill cs = CareerSkill.builder()
                .skill(skill)
                .priority(SkillPriority.CRITICAL)
                .requiredProficiency(ProficiencyLevel.BEGINNER)
                .isMandatory(true)
                .build();

        SkillGapItemResponse result = engine.evaluateSkillGap(cs, null);

        assertEquals(GapType.FULL_GAP, result.gapType());
        assertEquals(GapSeverity.CRITICAL, result.severity());
        assertEquals("NONE", result.currentProficiency());
        assertTrue(result.explanation().contains("Spring Boot is a critical required skill"));
    }

    @Test
    void evaluateSkillGapShouldIdentifyPartialGap() {
        Skill skill = Skill.builder()
                .name("Java")
                .category("Programming")
                .build();
        skill.setId(UUID.randomUUID());

        CareerSkill cs = CareerSkill.builder()
                .skill(skill)
                .priority(SkillPriority.CRITICAL)
                .requiredProficiency(ProficiencyLevel.INTERMEDIATE)
                .isMandatory(true)
                .build();

        UserSkill us = UserSkill.builder()
                .skill(skill)
                .proficiencyLevel(ProficiencyLevel.BEGINNER)
                .build();

        SkillGapItemResponse result = engine.evaluateSkillGap(cs, us);

        assertEquals(GapType.PARTIAL_GAP, result.gapType());
        assertEquals(GapSeverity.CRITICAL, result.severity());
        assertEquals("BEGINNER", result.currentProficiency());
        assertTrue(result.explanation().contains("Beginner level but the selected career requires Intermediate"));
    }

    @Test
    void evaluateSkillGapShouldIdentifyNoGap() {
        Skill skill = Skill.builder()
                .name("SQL")
                .category("Database")
                .build();
        skill.setId(UUID.randomUUID());

        CareerSkill cs = CareerSkill.builder()
                .skill(skill)
                .priority(SkillPriority.HIGH)
                .requiredProficiency(ProficiencyLevel.INTERMEDIATE)
                .isMandatory(true)
                .build();

        UserSkill us = UserSkill.builder()
                .skill(skill)
                .proficiencyLevel(ProficiencyLevel.INTERMEDIATE)
                .build();

        SkillGapItemResponse result = engine.evaluateSkillGap(cs, us);

        assertEquals(GapType.NO_GAP, result.gapType());
        assertEquals(GapSeverity.LOW, result.severity());
        assertEquals("INTERMEDIATE", result.currentProficiency());
        assertTrue(result.explanation().contains("meets or exceeds"));
    }

    @Test
    void calculateOverallGapScoreShouldReturnCorrectPercentage() {
        UUID skill1Id = UUID.randomUUID();
        UUID skill2Id = UUID.randomUUID();

        Skill skill1 = Skill.builder().name("Java").build();
        skill1.setId(skill1Id);

        Skill skill2 = Skill.builder().name("SQL").build();
        skill2.setId(skill2Id);

        CareerSkill cs1 = CareerSkill.builder()
                .skill(skill1)
                .priority(SkillPriority.HIGH)
                .requiredProficiency(ProficiencyLevel.INTERMEDIATE)
                .isMandatory(true)
                .build();

        CareerSkill cs2 = CareerSkill.builder()
                .skill(skill2)
                .priority(SkillPriority.HIGH)
                .requiredProficiency(ProficiencyLevel.INTERMEDIATE)
                .isMandatory(true)
                .build();

        UserSkill us2 = UserSkill.builder()
                .skill(skill2)
                .proficiencyLevel(ProficiencyLevel.INTERMEDIATE)
                .build();

        double score = engine.calculateOverallGapScore(List.of(cs1, cs2), Map.of(skill2Id, us2));

        assertEquals(50.0, score);
    }
}
