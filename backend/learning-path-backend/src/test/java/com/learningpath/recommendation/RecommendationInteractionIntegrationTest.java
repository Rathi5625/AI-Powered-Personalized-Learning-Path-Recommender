package com.learningpath.recommendation;

import com.learningpath.dto.CourseRequest;
import com.learningpath.dto.CourseResponse;
import com.learningpath.dto.UserCreateRequest;
import com.learningpath.dto.UserResponse;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.RecommendationInteractionType;
import com.learningpath.recommendation.dto.RecordRecommendationInteractionRequest;
import com.learningpath.recommendation.dto.RecommendationInteractionResponse;
import com.learningpath.recommendation.dto.UserInteractionStatsResponse;
import com.learningpath.recommendation.service.RecommendationInteractionService;
import com.learningpath.service.CourseService;
import com.learningpath.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_interactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class RecommendationInteractionIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private RecommendationInteractionService interactionService;

    @Test
    void testRecordAndRetrieveRecommendationInteractionsEndToEnd() {
        // 1. Create Learner & Course
        UserResponse user = userService.createUser(new UserCreateRequest(
                "Interaction Test User", "interact@example.org", "Java Developer", ExperienceLevel.BEGINNER, 2, null, null
        ));

        CourseResponse course = courseService.createCourse(new CourseRequest(
                "Spring Boot Masterclass", "Complete guide", "Udemy", "https://example.org/sb",
                CourseDifficulty.BEGINNER, 20.0, CourseType.VIDEO_COURSE, "English",
                new BigDecimal("4.80"), new BigDecimal("49.99"), false
        ));

        // 2. Record CLICKED Interaction with ML Score
        RecordRecommendationInteractionRequest clickedReq = new RecordRecommendationInteractionRequest(
                user.id(), course.id(), RecommendationInteractionType.CLICKED, 1, 87.4, 92.1, 89.28
        );
        RecommendationInteractionResponse clickedRes = interactionService.recordInteraction(clickedReq);

        assertNotNull(clickedRes.id());
        assertEquals(user.id(), clickedRes.userId());
        assertEquals(course.id(), clickedRes.courseId());
        assertEquals(RecommendationInteractionType.CLICKED, clickedRes.interactionType());
        assertEquals(87.4, clickedRes.ruleBasedScore());
        assertEquals(92.1, clickedRes.mlScore());
        assertEquals(89.28, clickedRes.finalScore());

        // 3. Record VIEWED Interaction with null ML Score (Fallback scenario)
        RecordRecommendationInteractionRequest viewedReq = new RecordRecommendationInteractionRequest(
                user.id(), course.id(), RecommendationInteractionType.VIEWED, 2, 80.0, null, 80.0
        );
        RecommendationInteractionResponse viewedRes = interactionService.recordInteraction(viewedReq);

        assertNotNull(viewedRes.id());
        assertNull(viewedRes.mlScore(), "mlScore can be null during ML service unavailability fallback");

        // 4. Retrieve User Interactions History
        List<RecommendationInteractionResponse> userHistory = interactionService.getUserInteractions(user.id());
        assertEquals(2, userHistory.size());

        // 5. Retrieve User Interaction Statistics
        UserInteractionStatsResponse stats = interactionService.getUserInteractionStats(user.id());
        assertNotNull(stats);
        assertEquals(2L, stats.totalInteractions());
        assertEquals(1L, stats.clicked());
        assertEquals(1L, stats.viewed());
        assertEquals(0L, stats.completed());
    }
}
