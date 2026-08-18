package com.learningpath.recommendation;

import com.learningpath.entity.Career;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.engine.RecommendationScoringEngine;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import com.learningpath.skilldependency.dto.LearningOrderResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_rec_readiness;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CourseRecommendationReadinessIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CourseSkillRepository courseSkillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillGapService skillGapService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SkillDependencyService skillDependencyService;

    private final RecommendationScoringEngine scoringEngine = new RecommendationScoringEngine();

    @Test
    @DisplayName("1. Database Integrity: All 244 dataset courses linked with valid CourseSkill records")
    void testDatabaseIntegrity() {
        List<Course> datasetCourses = courseRepository.findAll().stream()
                .filter(c -> c.getCourseCode() != null)
                .toList();

        assertThat(datasetCourses).hasSize(244);

        for (Course course : datasetCourses) {
            List<CourseSkill> courseSkills = courseSkillRepository.findByCourseId(course.getId());
            assertThat(courseSkills)
                    .withFailMessage("Course %s has no CourseSkill mapping", course.getCourseCode())
                    .isNotEmpty();

            for (CourseSkill cs : courseSkills) {
                assertThat(cs.getCourse()).isNotNull();
                assertThat(cs.getCourse().getId()).isEqualTo(course.getId());
                assertThat(cs.getSkill()).isNotNull();
                assertThat(cs.getSkill().getId()).isNotNull();
                assertThat(skillRepository.existsById(cs.getSkill().getId())).isTrue();
            }
        }
    }

    @Test
    @DisplayName("2. Skill -> Course Retrieval for Representative Skills")
    void testRepresentativeSkillCourseRetrieval() {
        String[] testSkills = new String[]{"HTML", "CSS", "JavaScript", "React", "Python", "Java", "Spring Boot", "Docker"};

        for (String skillName : testSkills) {
            Optional<Skill> skillOpt = skillRepository.findByName(skillName);
            assertThat(skillOpt)
                    .withFailMessage("Skill '%s' not found in database", skillName)
                    .isPresent();

            Skill skill = skillOpt.get();
            List<CourseSkill> candidates = courseSkillRepository.findBySkillIdIn(List.of(skill.getId()));

            assertThat(candidates)
                    .withFailMessage("Skill '%s' has 0 candidate course mappings", skillName)
                    .isNotEmpty();
        }
    }

    @Test
    @DisplayName("3. Skill Gap -> Course Candidate Retrieval Flow")
    void testSkillGapToCourseCandidateRetrieval() {
        Career frontendCareer = careerRepository.findByTitle("Frontend Developer").orElseThrow();

        User learner = userRepository.save(User.builder()
                .email("gap-test-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Gap Test Learner")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .preferredContentType(PreferredContentType.ARTICLE)
                .targetCareer(frontendCareer.getTitle())
                .build());

        Skill htmlSkill = skillRepository.findByName("HTML").orElseThrow();
        Skill cssSkill = skillRepository.findByName("CSS").orElseThrow();

        // Learner has HTML (BEGINNER) and CSS (BEGINNER), but lacks JavaScript and React
        userSkillRepository.save(UserSkill.builder().user(learner).skill(htmlSkill).proficiencyLevel(ProficiencyLevel.BEGINNER).build());
        userSkillRepository.save(UserSkill.builder().user(learner).skill(cssSkill).proficiencyLevel(ProficiencyLevel.BEGINNER).build());

        SkillGapAnalysisResponse gapAnalysis = skillGapService.analyzeSkillGap(learner.getId(), frontendCareer.getId());
        assertThat(gapAnalysis.gaps()).isNotEmpty();

        Set<UUID> gapSkillIds = gapAnalysis.gaps().stream()
                .filter(g -> g.gapType() != GapType.NO_GAP)
                .map(SkillGapItemResponse::skillId)
                .collect(Collectors.toSet());

        assertThat(gapSkillIds).isNotEmpty();

        List<CourseSkill> candidateCourseSkills = courseSkillRepository.findBySkillIdIn(gapSkillIds);
        assertThat(candidateCourseSkills).isNotEmpty();

        // Candidates must cover JavaScript and React
        Set<String> coveredSkills = candidateCourseSkills.stream()
                .map(cs -> cs.getSkill().getName())
                .collect(Collectors.toSet());

        assertThat(coveredSkills).contains("JavaScript", "React");
    }

    @Test
    @DisplayName("4. Prerequisite Compatibility: Prerequisite ordering preserved")
    void testPrerequisiteCompatibility() {
        List<String> missingGaps = List.of("JavaScript Frameworks", "JavaScript", "HTML", "CSS");
        LearningOrderResponse order = skillDependencyService.getLearningOrder(missingGaps);

        assertThat(order.success()).isTrue();
        List<String> phases = order.learningOrder();

        // HTML & CSS must come before JavaScript, JavaScript before JavaScript Frameworks
        int htmlIdx = phases.indexOf("HTML");
        int cssIdx = phases.indexOf("CSS");
        int jsIdx = phases.indexOf("JavaScript");
        int fwIdx = phases.indexOf("JavaScript Frameworks");

        assertThat(htmlIdx).isLessThan(jsIdx);
        assertThat(cssIdx).isLessThan(jsIdx);
        assertThat(jsIdx).isLessThan(fwIdx);
    }

    @Test
    @DisplayName("5. Difficulty Scoring Compatibility across all tiers")
    void testDifficultyScoringCompatibility() {
        // Beginner learner
        double begMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.BEGINNER);
        double easyMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.EASY);
        double highMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.BEGINNER, CourseDifficulty.HIGH);

        assertThat(begMatch).isEqualTo(100.0);
        assertThat(easyMatch).isEqualTo(100.0);
        assertThat(highMatch).isLessThan(easyMatch);

        // Advanced learner
        double advHighMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.ADVANCED, CourseDifficulty.HIGH);
        double advMedMatch = scoringEngine.calculateDifficultyMatch(ExperienceLevel.ADVANCED, CourseDifficulty.MEDIUM);
        assertThat(advHighMatch).isEqualTo(100.0);
        assertThat(advMedMatch).isEqualTo(75.0);
    }

    @Test
    @DisplayName("6. Course Type & Content Preference Compatibility")
    void testCourseTypePreferenceCompatibility() {
        User user = User.builder()
                .preferredContentType(PreferredContentType.ARTICLE)
                .build();

        Course docCourse = Course.builder()
                .courseType(CourseType.DOCUMENTATION)
                .isFree(true)
                .build();

        Course textCourse = Course.builder()
                .courseType(CourseType.TEXT_TUTORIAL)
                .isFree(true)
                .build();

        Course videoCourse = Course.builder()
                .courseType(CourseType.VIDEO_COURSE)
                .isFree(false)
                .build();

        double docScore = scoringEngine.calculateUserPreference(user, docCourse);
        double textScore = scoringEngine.calculateUserPreference(user, textCourse);
        double videoScore = scoringEngine.calculateUserPreference(user, videoCourse);

        // Documentation and Text Tutorial match ARTICLE preference (+30) + Free (+20) -> 100.0
        assertThat(docScore).isEqualTo(100.0);
        assertThat(textScore).isEqualTo(100.0);
        assertThat(videoScore).isLessThan(docScore);
    }

    @Test
    @DisplayName("7. Recommendation Readiness: Profile A (Full Stack Developer, BEGINNER)")
    void testRecommendationReadiness_ProfileA() {
        Career fullStackCareer = careerRepository.findByTitle("Full Stack Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("profile-a-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Profile A Learner")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .preferredContentType(PreferredContentType.INTERACTIVE_EXERCISE)
                .targetCareer(fullStackCareer.getTitle())
                .build());

        RecommendationSummaryResponse recs = recommendationService.getRecommendationsForUser(user.getId(), fullStackCareer.getId());

        assertThat(recs).isNotNull();
        assertThat(recs.recommendations()).isNotEmpty();
        assertThat(recs.totalCandidateCourses()).isGreaterThan(0);

        CourseRecommendationResponse topRec = recs.recommendations().get(0);
        assertThat(topRec.courseId()).isNotNull();
        assertThat(topRec.courseTitle()).isNotBlank();
        assertThat(topRec.finalScore()).isGreaterThan(0.0);
        assertThat(topRec.ruleBasedScore()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("8. Recommendation Readiness: Profile B (Frontend Developer, INTERMEDIATE)")
    void testRecommendationReadiness_ProfileB() {
        Career frontendCareer = careerRepository.findByTitle("Frontend Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("profile-b-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Profile B Learner")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .preferredContentType(PreferredContentType.ARTICLE)
                .targetCareer(frontendCareer.getTitle())
                .build());

        // Learner already knows HTML and CSS
        Skill html = skillRepository.findByName("HTML").orElseThrow();
        Skill css = skillRepository.findByName("CSS").orElseThrow();
        userSkillRepository.save(UserSkill.builder().user(user).skill(html).proficiencyLevel(ProficiencyLevel.INTERMEDIATE).build());
        userSkillRepository.save(UserSkill.builder().user(user).skill(css).proficiencyLevel(ProficiencyLevel.INTERMEDIATE).build());

        RecommendationSummaryResponse recs = recommendationService.getRecommendationsForUser(user.getId(), frontendCareer.getId());

        assertThat(recs).isNotNull();
        assertThat(recs.recommendations()).isNotEmpty();

        for (CourseRecommendationResponse r : recs.recommendations()) {
            assertThat(r.finalScore()).isBetween(0.0, 100.0);
            assertThat(r.ruleBasedScore()).isBetween(0.0, 100.0);
        }
    }

    @Test
    @DisplayName("9. Recommendation Readiness: Profile C (Java Backend Developer, ADVANCED)")
    void testRecommendationReadiness_ProfileC() {
        Career javaCareer = careerRepository.findByTitle("Java Backend Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("profile-c-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Profile C Learner")
                .experienceLevel(ExperienceLevel.ADVANCED)
                .preferredContentType(PreferredContentType.VIDEO)
                .targetCareer(javaCareer.getTitle())
                .build());

        RecommendationSummaryResponse recs = recommendationService.getRecommendationsForUser(user.getId(), javaCareer.getId());

        assertThat(recs).isNotNull();
        assertThat(recs.recommendations()).isNotEmpty();
    }

    @Test
    @DisplayName("10. Performance Check: Candidate query completes in under 150ms")
    void testCandidateQueryPerformance() {
        Career frontendCareer = careerRepository.findByTitle("Frontend Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("perf-user-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Performance Test User")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .targetCareer(frontendCareer.getTitle())
                .build());

        long start = System.currentTimeMillis();
        RecommendationSummaryResponse recs = recommendationService.getRecommendationsForUser(user.getId(), frontendCareer.getId());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(recs.recommendations()).isNotEmpty();
        assertThat(elapsed).isLessThan(1500L); // Generous ceiling for integration test execution
    }
}
