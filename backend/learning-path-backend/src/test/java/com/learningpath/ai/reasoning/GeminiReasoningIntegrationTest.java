package com.learningpath.ai.reasoning;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.reasoning.dto.*;
import com.learningpath.ai.reasoning.prompt.GeminiReasoningPromptBuilder;
import com.learningpath.ai.reasoning.service.GeminiReasoningService;
import com.learningpath.ai.reasoning.validation.GeminiReasoningValidator;
import com.learningpath.entity.Career;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_gemini_reasoning;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "gemini.api.key=mock-test-key",
        "gemini.model=gemini-1.5-flash"
})
@Transactional
class GeminiReasoningIntegrationTest {

    @Autowired
    private GeminiReasoningService geminiReasoningService;

    @Autowired
    private GeminiReasoningPromptBuilder promptBuilder;

    @Autowired
    private GeminiReasoningValidator validator;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CareerRepository careerRepository;

    @MockitoBean
    private GeminiClient geminiClient;

    @Test
    @DisplayName("1. Prompt Construction: Serializes validated context without private data")
    void testPromptConstruction() {
        LearnerProfileDto learner = new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE");
        CandidateCourseDto course = new CandidateCourseDto(
                UUID.randomUUID(), "FE_01_01", "MDN Web Basics", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("HTML"), List.of("HTML"), 90.0, 85.0, 88.5
        );
        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(), "HTML", "Frontend", "NOVICE", ProficiencyLevel.INTERMEDIATE,
                GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.CRITICAL, true, "Missing"
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                learner, List.of(gap), List.of(course), List.of("Internet Basics", "HTML", "CSS")
        );

        String prompt = promptBuilder.buildReasoningPrompt(input);

        assertThat(prompt).contains("MDN Web Basics");
        assertThat(prompt).contains("FE_01_01");
        assertThat(prompt).contains("Frontend Developer");
        assertThat(prompt).contains("CRITICAL ANTI-HALLUCINATION & INTEGRITY RULES");
    }

    @Test
    @DisplayName("2. Structured Response Parsing & Validation: Valid Gemini output mapped accurately")
    void testValidGeminiResponseParsing() {
        UUID validCourseId = UUID.randomUUID();
        CandidateCourseDto course = new CandidateCourseDto(
                validCourseId, "FE_02_01", "MDN HTML", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("HTML"), List.of("HTML"), 95.0, 90.0, 93.5
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE"),
                List.of(),
                List.of(course),
                List.of("HTML")
        );

        String mockAiResponseJson = """
                ```json
                {
                  "summary": "Tailored HTML foundational roadmap for Frontend Developer.",
                  "recommendations": [
                    {
                      "courseId": "%s",
                      "reason": "Essential prerequisite for semantic web structure.",
                      "skillsAddressed": ["HTML"],
                      "gapSkillsAddressed": ["HTML"],
                      "prerequisiteReason": "Foundational first step.",
                      "estimatedEffort": "3 hours",
                      "priority": 1
                    }
                  ],
                  "learningSequence": [
                    {
                      "courseId": "%s",
                      "order": 1,
                      "reason": "Complete first."
                    }
                  ],
                  "adaptationNotes": ["Path will unlock CSS upon HTML completion."]
                }
                ```
                """.formatted(validCourseId, validCourseId);

        when(geminiClient.generateContent(anyString()))
                .thenReturn(AiTestResponse.ok("gemini-1.5-flash", mockAiResponseJson));

        GeminiReasoningResult result = geminiReasoningService.generateReasoning(input);

        assertThat(result).isNotNull();
        assertThat(result.isAiGenerated()).isTrue();
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations().get(0).courseId()).isEqualTo(validCourseId);
        assertThat(result.recommendations().get(0).reason()).contains("Essential prerequisite");
    }

    @Test
    @DisplayName("3. Anti-Hallucination: Reject and prune unrecognized course IDs")
    void testAntiHallucinationPruning() {
        UUID realCourseId = UUID.randomUUID();
        UUID fakeCourseId = UUID.randomUUID();

        CandidateCourseDto realCourse = new CandidateCourseDto(
                realCourseId, "FE_02_01", "Real Course", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("HTML"), List.of("HTML"), 90.0, null, 90.0
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE"),
                List.of(),
                List.of(realCourse),
                List.of("HTML")
        );

        // Gemini attempts to return both real and fake course IDs
        GeminiReasoningResult rawResult = new GeminiReasoningResult(
                "Test summary",
                List.of(
                        new GeminiCourseExplanationDto(realCourseId, "Real explanation", List.of("HTML"), List.of("HTML"), "prereq", "3h", 1),
                        new GeminiCourseExplanationDto(fakeCourseId, "Hallucinated course", List.of("FakeSkill"), List.of(), "prereq", "10h", 2)
                ),
                List.of(
                        new GeminiCourseSequenceItemDto(realCourseId, 1, "Real step"),
                        new GeminiCourseSequenceItemDto(fakeCourseId, 2, "Fake step")
                ),
                List.of(),
                true
        );

        GeminiReasoningResult sanitized = validator.validateAndSanitize(rawResult, input);

        assertThat(sanitized).isNotNull();
        assertThat(sanitized.recommendations()).hasSize(1);
        assertThat(sanitized.recommendations().get(0).courseId()).isEqualTo(realCourseId);
        assertThat(sanitized.learningSequence()).hasSize(1);
        assertThat(sanitized.learningSequence().get(0).courseId()).isEqualTo(realCourseId);
    }

    @Test
    @DisplayName("4. Prerequisite Sequence Validation: Inverted course orders are automatically corrected")
    void testPrerequisiteSequenceCorrection() {
        UUID htmlCourseId = UUID.randomUUID();
        UUID jsCourseId = UUID.randomUUID();

        CandidateCourseDto htmlCourse = new CandidateCourseDto(
                htmlCourseId, "FE_02_01", "HTML Course", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("HTML"), List.of("HTML"), 90.0, null, 90.0
        );
        CandidateCourseDto jsCourse = new CandidateCourseDto(
                jsCourseId, "FE_04_01", "JS Course", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("JavaScript"), List.of("JavaScript"), 85.0, null, 85.0
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE"),
                List.of(),
                List.of(htmlCourse, jsCourse),
                List.of("HTML", "JavaScript") // HTML must precede JavaScript
        );

        // Inverted sequence: Gemini wrongly puts JS (step 1) before HTML (step 2)
        GeminiReasoningResult rawResult = new GeminiReasoningResult(
                "Test summary",
                List.of(
                        new GeminiCourseExplanationDto(jsCourseId, "JS first", List.of("JavaScript"), List.of("JavaScript"), "", "3h", 1),
                        new GeminiCourseExplanationDto(htmlCourseId, "HTML second", List.of("HTML"), List.of("HTML"), "", "3h", 2)
                ),
                List.of(
                        new GeminiCourseSequenceItemDto(jsCourseId, 1, "JS first"),
                        new GeminiCourseSequenceItemDto(htmlCourseId, 2, "HTML second")
                ),
                List.of(),
                true
        );

        GeminiReasoningResult sanitized = validator.validateAndSanitize(rawResult, input);

        assertThat(sanitized).isNotNull();
        assertThat(sanitized.learningSequence()).hasSize(2);
        // Step 1 must be HTML, Step 2 must be JS
        assertThat(sanitized.learningSequence().get(0).courseId()).isEqualTo(htmlCourseId);
        assertThat(sanitized.learningSequence().get(1).courseId()).isEqualTo(jsCourseId);
    }

    @Test
    @DisplayName("5. Resilient Fallback: Invalid JSON or API failure triggers deterministic explanation")
    void testResilientFallbackOnApiFailure() {
        UUID courseId = UUID.randomUUID();
        CandidateCourseDto course = new CandidateCourseDto(
                courseId, "FE_01_01", "MDN Basics", "MDN", "BEGINNER", "DOCUMENTATION",
                List.of("Internet Basics"), List.of("Internet Basics"), 90.0, null, 90.0
        );

        SkillGapItemResponse gap = new SkillGapItemResponse(
                UUID.randomUUID(), "Internet Basics", "Frontend", "NOVICE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, true, "Missing"
        );

        GeminiReasoningInput input = new GeminiReasoningInput(
                new LearnerProfileDto("Frontend Developer", "BEGINNER", 2.0, "VISUAL", "ARTICLE"),
                List.of(gap),
                List.of(course),
                List.of("Internet Basics")
        );

        // Mock failure from GeminiClient
        when(geminiClient.generateContent(anyString()))
                .thenReturn(AiTestResponse.fail("gemini-1.5-flash", "HTTP 503 Service Unavailable"));

        GeminiReasoningResult result = geminiReasoningService.generateReasoning(input);

        assertThat(result).isNotNull();
        assertThat(result.isAiGenerated()).isFalse(); // Fallback indicator
        assertThat(result.recommendations()).hasSize(1);
        assertThat(result.recommendations().get(0).courseId()).isEqualTo(courseId);
        assertThat(result.recommendations().get(0).reason()).contains("Directly addresses your FULL_GAP gap in Internet Basics");
    }

    @Test
    @DisplayName("6. End-to-End Recommendation with Gemini Reasoning: Service delivers enhanced response")
    void testEndToEndRecommendationEnhancement() {
        Career frontendCareer = careerRepository.findByTitle("Frontend Developer").orElseThrow();

        User user = userRepository.save(User.builder()
                .email("gemini-test-" + UUID.randomUUID() + "@example.com")
                .passwordHash("hashed")
                .fullName("Gemini Test User")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .preferredContentType(PreferredContentType.ARTICLE)
                .targetCareer(frontendCareer.getTitle())
                .build());

        RecommendationSummaryResponse response = recommendationService.getRecommendationsForUser(user.getId(), frontendCareer.getId());

        assertThat(response).isNotNull();
        assertThat(response.recommendations()).isNotEmpty();

        for (CourseRecommendationResponse rec : response.recommendations()) {
            assertThat(rec.explanation()).isNotBlank();
            assertThat(rec.courseTitle()).isNotBlank();
            assertThat(rec.finalScore()).isGreaterThan(0.0);
        }
    }
}
