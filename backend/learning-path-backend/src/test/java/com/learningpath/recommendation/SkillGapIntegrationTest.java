package com.learningpath.recommendation;

import com.learningpath.dto.CareerResponse;
import com.learningpath.dto.SkillResponse;
import com.learningpath.dto.UserCreateRequest;
import com.learningpath.dto.UserResponse;
import com.learningpath.dto.UserSkillRequest;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.service.CareerService;
import com.learningpath.service.SkillService;
import com.learningpath.service.UserService;
import com.learningpath.service.UserSkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class SkillGapIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private UserSkillService userSkillService;

    @Autowired
    private CareerService careerService;

    @Autowired
    private SkillGapService skillGapService;

    @Test
    void testEndToEndSkillGapAnalysisForJavaBackendDeveloper() {
        // 1. Create Test User
        UserCreateRequest userReq = new UserCreateRequest(
                "Gap Test Learner",
                "gap.test." + UUID.randomUUID() + "@example.com",
                "Wants to become a Java Backend Developer",
                ExperienceLevel.BEGINNER,
                2,
                LearningStyle.VISUAL,
                PreferredContentType.VIDEO
        );
        UserResponse user = userService.createUser(userReq);

        // 2. Find Java Backend Developer Career (created by CareerDataInitializer)
        List<CareerResponse> careers = careerService.searchCareersByName("Java Backend Developer");
        assertFalse(careers.isEmpty(), "Java Backend Developer career should exist in seed data");
        CareerResponse career = careers.get(0);

        // 3. Find Skills: Java, OOP, SQL
        List<SkillResponse> javaSkills = skillService.searchSkillsByName("Java");
        List<SkillResponse> oopSkills = skillService.searchSkillsByName("OOP");
        List<SkillResponse> sqlSkills = skillService.searchSkillsByName("SQL");

        assertFalse(javaSkills.isEmpty(), "Java skill should exist");
        assertFalse(oopSkills.isEmpty(), "OOP skill should exist");
        assertFalse(sqlSkills.isEmpty(), "SQL skill should exist");

        SkillResponse javaSkill = javaSkills.stream().filter(s -> s.name().equalsIgnoreCase("Java")).findFirst().orElseThrow();
        SkillResponse oopSkill = oopSkills.stream().filter(s -> s.name().equalsIgnoreCase("OOP")).findFirst().orElseThrow();
        SkillResponse sqlSkill = sqlSkills.stream().filter(s -> s.name().equalsIgnoreCase("SQL")).findFirst().orElseThrow();

        // 4. Assign Skills to User:
        // Java -> BEGINNER (Required: INTERMEDIATE => PARTIAL_GAP)
        userSkillService.addUserSkill(user.id(), new UserSkillRequest(
                javaSkill.id(),
                ProficiencyLevel.BEGINNER,
                new BigDecimal("70.0"),
                SkillSource.SELF_REPORTED
        ));

        // OOP -> BEGINNER (Required: INTERMEDIATE => PARTIAL_GAP)
        userSkillService.addUserSkill(user.id(), new UserSkillRequest(
                oopSkill.id(),
                ProficiencyLevel.BEGINNER,
                new BigDecimal("65.0"),
                SkillSource.SELF_REPORTED
        ));

        // SQL -> INTERMEDIATE (Required: INTERMEDIATE => NO_GAP)
        userSkillService.addUserSkill(user.id(), new UserSkillRequest(
                sqlSkill.id(),
                ProficiencyLevel.INTERMEDIATE,
                new BigDecimal("85.0"),
                SkillSource.SELF_REPORTED
        ));

        // 5. Execute Skill Gap Analysis Engine
        SkillGapAnalysisResponse analysis = skillGapService.analyzeSkillGap(user.id(), career.id());

        // 6. Verify Results
        assertNotNull(analysis);
        assertEquals("Java Backend Developer", analysis.careerName());
        assertEquals(10, analysis.totalRequiredSkills());
        assertEquals(1, analysis.skillsWithNoGap(), "1 skill (SQL) meets requirements");
        assertEquals(2, analysis.partialGaps(), "2 skills (Java, OOP) have partial gaps");
        assertEquals(7, analysis.fullGaps(), "7 skills are missing completely");
        assertTrue(analysis.overallGapScore() > 0.0, "Overall gap score should reflect deficiency");

        // Check SQL item (NO_GAP)
        SkillGapItemResponse sqlItem = analysis.gaps().stream()
                .filter(g -> g.skillName().equalsIgnoreCase("SQL"))
                .findFirst().orElseThrow();
        assertEquals(GapType.NO_GAP, sqlItem.gapType());

        // Check Java item (PARTIAL_GAP)
        SkillGapItemResponse javaItem = analysis.gaps().stream()
                .filter(g -> g.skillName().equalsIgnoreCase("Java"))
                .findFirst().orElseThrow();
        assertEquals(GapType.PARTIAL_GAP, javaItem.gapType());
        assertTrue(javaItem.explanation().contains("Beginner level but the selected career requires Intermediate"));

        // Check Spring Boot item (FULL_GAP)
        SkillGapItemResponse springBootItem = analysis.gaps().stream()
                .filter(g -> g.skillName().equalsIgnoreCase("Spring Boot"))
                .findFirst().orElseThrow();
        assertEquals(GapType.FULL_GAP, springBootItem.gapType());
        assertTrue(springBootItem.explanation().contains("Spring Boot is a critical required skill"));
    }
}
