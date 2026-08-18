package com.learningpath.recommendation;

import com.learningpath.entity.Career;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.engine.RecommendationScoringEngine;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_ml_hybrid;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "recommendation.scoring.rule-weight=0.70",
        "recommendation.scoring.ml-weight=0.30"
})
@Transactional
class MlHybridRankingIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSkillRepository courseSkillRepository;

    private final RecommendationScoringEngine scoringEngine = new RecommendationScoringEngine();

    @Test
    @DisplayName("1. Feature Extraction: buildMlPredictionRequest generates valid 10-feature vector")
    void testFeatureExtraction() {
        User user = User.builder()
                .experienceLevel(ExperienceLevel.BEGINNER)
                .preferredContentType(PreferredContentType.ARTICLE)
                .build();

        Skill htmlSkill = Skill.builder().name("HTML").build();
        Course course = Course.builder()
                .title("MDN HTML Basics")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.DOCUMENTATION)
                .rating(BigDecimal.valueOf(4.8))
                .durationHours(3.0)
                .isFree(true)
                .build();

        CourseSkill cs = CourseSkill.builder()
                .course(course)
                .skill(htmlSkill)
                .coverageLevel(CoverageLevel.ADVANCED)
                .importance(SkillPriority.CRITICAL)
                .targetProficiency(ProficiencyLevel.INTERMEDIATE)
                .build();

        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(),
                "HTML",
                "Frontend",
                "NOVICE",
                ProficiencyLevel.INTERMEDIATE,
                GapType.FULL_GAP,
                GapSeverity.HIGH,
                SkillPriority.CRITICAL,
                true,
                "Skill is missing"
        );

        Map<String, SkillGapItemResponse> gapMap = Map.of("HTML", gap);

        MlPredictionRequest request = scoringEngine.buildMlPredictionRequest(course, List.of(cs), gapMap, user);

        assertThat(request).isNotNull();
        assertThat(request.skillGapScore()).isBetween(0.0, 1.0);
        assertThat(request.careerPriorityScore()).isBetween(0.0, 1.0);
        assertThat(request.skillCoverage()).isBetween(0.0, 1.0);
        assertThat(request.proficiencyGap()).isBetween(0.0, 1.0);
        assertThat(request.difficultyMatch()).isEqualTo(1.0); // Beginner learner + Beginner course
        assertThat(request.courseRating()).isCloseTo(0.96, within(0.01)); // 4.8 / 5.0
        assertThat(request.preferenceMatch()).isEqualTo(1.0); // Article + Free match
        assertThat(request.mandatorySkillMatch()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("2. Hybrid Scoring Formula: (0.70 * ruleScore) + (0.30 * mlScore)")
    void testHybridScoreFormula() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        Course course = Course.builder().title("Sample Course").difficulty(CourseDifficulty.BEGINNER).rating(BigDecimal.valueOf(4.5)).build();
        Skill skill = Skill.builder().name("CSS").build();
        CourseSkill cs = CourseSkill.builder().course(course).skill(skill).importance(SkillPriority.HIGH).build();

        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(),
                "CSS",
                "Frontend",
                "NOVICE",
                ProficiencyLevel.INTERMEDIATE,
                GapType.FULL_GAP,
                GapSeverity.HIGH,
                SkillPriority.HIGH,
                true,
                "Skill is missing"
        );

        Map<String, SkillGapItemResponse> gapMap = Map.of("CSS", gap);

        // When ML score is 80.0, rule score is calculated
        CourseRecommendationResponse rec = scoringEngine.scoreAndBuildRecommendation(
                1, course, List.of(cs), gapMap, user, 80.0, 0.70, 0.30
        );

        double expectedFinal = Math.round(((rec.ruleBasedScore() * 0.70) + (80.0 * 0.30)) * 10.0) / 10.0;
        assertThat(rec.finalScore()).isEqualTo(expectedFinal);
        assertThat(rec.mlScore()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("3. Cold Start Fallback: When ML score is null, finalScore equals ruleBasedScore")
    void testColdStartFallback() {
        User user = User.builder().experienceLevel(ExperienceLevel.BEGINNER).build();
        Course course = Course.builder().title("Sample Course").difficulty(CourseDifficulty.BEGINNER).rating(BigDecimal.valueOf(4.0)).build();
        Skill skill = Skill.builder().name("JavaScript").build();
        CourseSkill cs = CourseSkill.builder().course(course).skill(skill).importance(SkillPriority.HIGH).build();

        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(),
                "JavaScript",
                "Frontend",
                "NOVICE",
                ProficiencyLevel.INTERMEDIATE,
                GapType.FULL_GAP,
                GapSeverity.HIGH,
                SkillPriority.HIGH,
                true,
                "Skill is missing"
        );

        Map<String, SkillGapItemResponse> gapMap = Map.of("JavaScript", gap);

        // Null ML score (service unavailable or cold start)
        CourseRecommendationResponse rec = scoringEngine.scoreAndBuildRecommendation(
                1, course, List.of(cs), gapMap, user, null, 0.70, 0.30
        );

        assertThat(rec.finalScore()).isEqualTo(rec.ruleBasedScore());
        assertThat(rec.mlScore()).isNull();
    }

    @Test
    @DisplayName("4. End-to-End Fallback Safety: Service delivers complete recommendations when ML is offline")
    void testEndToEndOfflineSafety() {
        Career frontendCareer = careerRepository.findByTitle("Frontend Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("offline-ml-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Offline ML Test User")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .targetCareer(frontendCareer.getTitle())
                .build());

        RecommendationSummaryResponse recs = recommendationService.getRecommendationsForUser(user.getId(), frontendCareer.getId());

        assertThat(recs).isNotNull();
        assertThat(recs.recommendations()).isNotEmpty();

        for (CourseRecommendationResponse rec : recs.recommendations()) {
            assertThat(rec.finalScore()).isGreaterThan(0.0);
            assertThat(rec.ruleBasedScore()).isGreaterThan(0.0);
            assertThat(rec.courseTitle()).isNotBlank();
        }
    }
}
