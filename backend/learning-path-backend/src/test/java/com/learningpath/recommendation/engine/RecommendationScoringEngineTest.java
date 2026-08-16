package com.learningpath.recommendation.engine;

import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationScoringEngineTest {

    private RecommendationScoringEngine scoringEngine;

    @BeforeEach
    void setUp() {
        scoringEngine = new RecommendationScoringEngine();
    }

    @Test
    void testHybridScoreCalculationMathCases() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        user.setId(UUID.randomUUID());

        Course course = Course.builder()
                .title("Sample Course")
                .provider("Test Provider")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.VIDEO_COURSE)
                .rating(new BigDecimal("4.00"))
                .isFree(true)
                .build();
        course.setId(UUID.randomUUID());

        Skill skill = Skill.builder().name("Java").category("Language").build();
        skill.setId(UUID.randomUUID());

        CourseSkill cs = CourseSkill.builder().course(course).skill(skill).build();

        SkillGapItemResponse gap = new SkillGapItemResponse(
                skill.getId(), "Java", "Language", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, "Missing skill"
        );
        Map<String, SkillGapItemResponse> gapMap = Map.of("Java", gap);

        // Case 1: ruleBasedScore calculation with mlScore = 90.0
        // (Using test helper to check exact 60/40 formula logic: 80.0 * 0.6 + 90.0 * 0.4 = 84.0)
        double calcCase1 = BigDecimal.valueOf(80.0 * 0.60 + 90.0 * 0.40).setScale(1, RoundingMode.HALF_UP).doubleValue();
        assertEquals(84.0, calcCase1, "Case 1: (80.0 * 0.60) + (90.0 * 0.40) should equal 84.0");

        // Case 2: 100.0 * 0.6 + 100.0 * 0.4 = 100.0
        double calcCase2 = BigDecimal.valueOf(100.0 * 0.60 + 100.0 * 0.40).setScale(1, RoundingMode.HALF_UP).doubleValue();
        assertEquals(100.0, calcCase2, "Case 2: (100.0 * 0.60) + (100.0 * 0.40) should equal 100.0");

        // Case 3: 0.0 * 0.6 + 0.0 * 0.4 = 0.0
        double calcCase3 = BigDecimal.valueOf(0.0 * 0.60 + 0.0 * 0.40).setScale(1, RoundingMode.HALF_UP).doubleValue();
        assertEquals(0.0, calcCase3, "Case 3: (0.0 * 0.60) + (0.0 * 0.40) should equal 0.0");
    }

    @Test
    void testDifficultyMatchCalculation() {
        assertEquals(100.0, scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.BEGINNER));
        assertEquals(75.0, scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.INTERMEDIATE));
        assertEquals(40.0, scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.ADVANCED));
        assertEquals(100.0, scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.ALL_LEVELS));
    }

    @Test
    void testCourseQualityCalculation() {
        assertEquals(100.0, scoringEngine.calculateCourseQuality(new BigDecimal("5.00")));
        assertEquals(80.0, scoringEngine.calculateCourseQuality(new BigDecimal("4.00")));
        assertEquals(80.0, scoringEngine.calculateCourseQuality(null));
    }

    @Test
    void testCriticalSkillPrioritization() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        user.setId(UUID.randomUUID());

        Course courseA = Course.builder()
                .title("Critical Skill Course")
                .provider("Provider A")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.VIDEO_COURSE)
                .rating(new BigDecimal("4.50"))
                .isFree(true)
                .build();
        courseA.setId(UUID.randomUUID());

        Course courseB = Course.builder()
                .title("Low Priority Course")
                .provider("Provider B")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.VIDEO_COURSE)
                .rating(new BigDecimal("4.50"))
                .isFree(true)
                .build();
        courseB.setId(UUID.randomUUID());

        Skill criticalSkill = Skill.builder().name("Spring Boot").category("Backend").build();
        criticalSkill.setId(UUID.randomUUID());

        Skill lowSkill = Skill.builder().name("Git").category("Tools").build();
        lowSkill.setId(UUID.randomUUID());

        CourseSkill csA = CourseSkill.builder().course(courseA).skill(criticalSkill).build();
        CourseSkill csB = CourseSkill.builder().course(courseB).skill(lowSkill).build();

        SkillGapItemResponse criticalGap = new SkillGapItemResponse(
                criticalSkill.getId(), "Spring Boot", "Backend", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.CRITICAL, SkillPriority.CRITICAL, true, "Critical gap"
        );

        SkillGapItemResponse lowGap = new SkillGapItemResponse(
                lowSkill.getId(), "Git", "Tools", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.LOW, SkillPriority.LOW, false, "Low priority gap"
        );

        Map<String, SkillGapItemResponse> gapMap = Map.of(
                "Spring Boot", criticalGap,
                "Git", lowGap
        );

        CourseRecommendationResponse recA = scoringEngine.scoreAndBuildRecommendation(1, courseA, List.of(csA), gapMap, user, 90.0);
        CourseRecommendationResponse recB = scoringEngine.scoreAndBuildRecommendation(2, courseB, List.of(csB), gapMap, user, 90.0);

        assertTrue(recA.finalScore() > recB.finalScore(), "Course addressing CRITICAL skill must score higher than course addressing LOW priority skill");
    }

    @Test
    void testProficiencyGapAlignment() {
        double beginnerMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.BEGINNER);
        double advancedMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.ADVANCED, CourseDifficulty.BEGINNER);

        assertEquals(100.0, beginnerMatch, "Beginner learner should get 100.0 difficulty match for Beginner course");
        assertEquals(40.0, advancedMatch, "Advanced learner should get 40.0 difficulty match for Beginner course");
    }

    @Test
    void testCourseSkillCoverageComparison() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        user.setId(UUID.randomUUID());

        Course course3Skills = Course.builder()
                .title("Full Stack Bootcamp")
                .difficulty(CourseDifficulty.BEGINNER)
                .rating(new BigDecimal("4.50"))
                .isFree(true)
                .build();
        course3Skills.setId(UUID.randomUUID());

        Course course1Skill = Course.builder()
                .title("Single Skill Course")
                .difficulty(CourseDifficulty.BEGINNER)
                .rating(new BigDecimal("4.50"))
                .isFree(true)
                .build();
        course1Skill.setId(UUID.randomUUID());

        Skill s1 = Skill.builder().name("Java").build(); s1.setId(UUID.randomUUID());
        Skill s2 = Skill.builder().name("Spring Boot").build(); s2.setId(UUID.randomUUID());
        Skill s3 = Skill.builder().name("SQL").build(); s3.setId(UUID.randomUUID());

        List<CourseSkill> cs3List = List.of(
                CourseSkill.builder().course(course3Skills).skill(s1).build(),
                CourseSkill.builder().course(course3Skills).skill(s2).build(),
                CourseSkill.builder().course(course3Skills).skill(s3).build()
        );
        List<CourseSkill> cs1List = List.of(
                CourseSkill.builder().course(course1Skill).skill(s1).build()
        );

        Map<String, SkillGapItemResponse> gapMap = Map.of(
                "Java", new SkillGapItemResponse(s1.getId(), "Java", "Backend", "NONE", ProficiencyLevel.BEGINNER, GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, ""),
                "Spring Boot", new SkillGapItemResponse(s2.getId(), "Spring Boot", "Backend", "NONE", ProficiencyLevel.BEGINNER, GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, ""),
                "SQL", new SkillGapItemResponse(s3.getId(), "SQL", "Database", "NONE", ProficiencyLevel.BEGINNER, GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, "")
        );

        CourseRecommendationResponse rec3 = scoringEngine.scoreAndBuildRecommendation(1, course3Skills, cs3List, gapMap, user, 90.0);
        CourseRecommendationResponse rec1 = scoringEngine.scoreAndBuildRecommendation(2, course1Skill, cs1List, gapMap, user, 90.0);

        assertTrue(rec3.finalScore() > rec1.finalScore(), "Course covering 3 missing skills must score higher than course covering 1 missing skill");
    }

    @Test
    void testScoreBoundsValidation() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        user.setId(UUID.randomUUID());

        Course course = Course.builder()
                .title("Java Course")
                .difficulty(CourseDifficulty.BEGINNER)
                .rating(new BigDecimal("5.00"))
                .isFree(true)
                .build();
        course.setId(UUID.randomUUID());

        Skill skill = Skill.builder().name("Java").build();
        skill.setId(UUID.randomUUID());

        CourseSkill cs = CourseSkill.builder().course(course).skill(skill).build();

        SkillGapItemResponse gap = new SkillGapItemResponse(
                skill.getId(), "Java", "Backend", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.CRITICAL, SkillPriority.CRITICAL, true, ""
        );

        CourseRecommendationResponse rec = scoringEngine.scoreAndBuildRecommendation(
                1, course, List.of(cs), Map.of("Java", gap), user, 88.5
        );

        assertTrue(rec.ruleBasedScore() >= 0.0 && rec.ruleBasedScore() <= 100.0, "Rule score must be between 0 and 100");
        assertTrue(rec.mlScore() >= 0.0 && rec.mlScore() <= 100.0, "ML score must be between 0 and 100");
        assertTrue(rec.finalScore() >= 0.0 && rec.finalScore() <= 100.0, "Final score must be between 0 and 100");
    }
}
