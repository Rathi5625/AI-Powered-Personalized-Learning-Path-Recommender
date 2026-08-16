package com.learningpath.recommendation;

import com.learningpath.dto.UserCreateRequest;
import com.learningpath.dto.UserResponse;
import com.learningpath.dto.UserSkillRequest;
import com.learningpath.entity.Career;
import com.learningpath.entity.Skill;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.service.UserService;
import com.learningpath.service.UserSkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_rec;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "ml.service.url=http://localhost:8000"
})
@Transactional
class RecommendationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSkillService userSkillService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private RecommendationService recommendationService;

    @Test
    void testRecommendationEngineEndToEnd() {
        // 1. Create Learner Profile
        UserCreateRequest userRequest = new UserCreateRequest(
                "Alice Dev",
                "alice.rec@example.org",
                "Java Backend Developer",
                ExperienceLevel.BEGINNER,
                2,
                null,
                null
        );
        UserResponse user = userService.createUser(userRequest);

        // 2. Map Current Skills (Java: BEGINNER, OOP: BEGINNER, SQL: INTERMEDIATE)
        Skill java = skillRepository.findByName("Java").orElseThrow();
        Skill oop = skillRepository.findByName("OOP").orElseThrow();
        Skill sql = skillRepository.findByName("SQL").orElseThrow();

        userSkillService.addUserSkill(user.id(), new UserSkillRequest(java.getId(), ProficiencyLevel.BEGINNER, new BigDecimal("80.0"), SkillSource.SELF_REPORTED));
        userSkillService.addUserSkill(user.id(), new UserSkillRequest(oop.getId(), ProficiencyLevel.BEGINNER, new BigDecimal("80.0"), SkillSource.SELF_REPORTED));
        userSkillService.addUserSkill(user.id(), new UserSkillRequest(sql.getId(), ProficiencyLevel.INTERMEDIATE, new BigDecimal("90.0"), SkillSource.SELF_REPORTED));

        // 3. Find target career "Java Backend Developer"
        Career career = careerRepository.findAll().stream()
                .filter(c -> c.getTitle().equalsIgnoreCase("Java Backend Developer"))
                .findFirst()
                .orElseThrow();

        // 4. Generate Course Recommendations
        RecommendationSummaryResponse summary = recommendationService.getRecommendationsForUser(
                user.id(), career.getId()
        );

        // 5. Assertions
        assertNotNull(summary);
        assertTrue(summary.hasGaps(), "User should have skill gaps for Java Backend Developer career");
        assertFalse(summary.recommendations().isEmpty(), "Engine should recommend candidate courses");

        // Verify ordering by finalScore descending
        double prevFinalScore = 100.0;
        int rank = 1;
        for (CourseRecommendationResponse rec : summary.recommendations()) {
            assertEquals(rank, rec.rank(), "Ranks should be sequential 1..N");
            assertTrue(rec.finalScore() <= prevFinalScore, "Recommendations should be sorted by finalScore descending");
            assertTrue(rec.finalScore() >= 0.0 && rec.finalScore() <= 100.0, "finalScore must be bounded between 0 and 100");
            assertNotNull(rec.ruleBasedScore(), "ruleBasedScore must be present");
            assertNotNull(rec.explanation(), "Explanation must be populated");
            assertFalse(rec.gapSkillsAddressed().isEmpty(), "Recommended course must address at least one gap skill");
            prevFinalScore = rec.finalScore();
            rank++;
        }
    }
}
