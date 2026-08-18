package com.learningpath.ai.reasoning;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.reasoning.dto.CandidateCourseDto;
import com.learningpath.ai.reasoning.dto.GeminiReasoningInput;
import com.learningpath.ai.reasoning.dto.GeminiReasoningResult;
import com.learningpath.ai.reasoning.dto.LearnerProfileDto;
import com.learningpath.ai.reasoning.service.GeminiReasoningService;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_gemini_live;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiLiveApiIntegrationTest {

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private GeminiReasoningService geminiReasoningService;

    @Test
    @DisplayName("Live API: Test direct connectivity to Google Gemini API")
    void testLiveConnectivity() {
        AiTestResponse response = geminiClient.testConnectivity();
        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.response()).isNotBlank();
    }

    @Test
    @DisplayName("Live API: Test grounded reasoning generation against Google Gemini API")
    void testLiveReasoningGeneration() {
        UUID courseId = UUID.randomUUID();
        CandidateCourseDto course = new CandidateCourseDto(
                courseId, "FE_02_01", "MDN HTML Guide", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("HTML"), List.of("HTML"), 95.0, 90.0, 93.5
        );

        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(), "HTML", "Frontend", "NOVICE", ProficiencyLevel.INTERMEDIATE,
                GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.CRITICAL, true, "Missing mandatory skill"
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE"),
                List.of(gap),
                List.of(course),
                List.of("HTML", "CSS", "JavaScript")
        );

        GeminiReasoningResult result = geminiReasoningService.generateReasoning(input);

        assertThat(result).isNotNull();
        assertThat(result.recommendations()).isNotEmpty();
        assertThat(result.recommendations().get(0).courseId()).isEqualTo(courseId);
    }
}
