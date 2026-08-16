package com.learningpath.learningpath.service;

import com.learningpath.ai.dto.AiOperation;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.service.AiService;
import com.learningpath.ai.validation.LearningPathValidator;
import com.learningpath.ai.validation.ValidationResult;
import com.learningpath.entity.Career;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.learningpath.dto.LearningPathContext;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import com.learningpath.skilldependency.dto.LearningOrderResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalizedLearningPathServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private SkillGapService skillGapService;
    @Mock
    private SkillDependencyService dependencyService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private LearningPathPromptBuilder promptBuilder;
    @Mock
    private AiService aiService;
    @Mock
    private LearningPathValidator validator;

    private PersonalizedLearningPathService learningPathService;

    private UUID userId;
    private UUID careerId;
    private User testUser;
    private Career testCareer;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        learningPathService = new PersonalizedLearningPathService(
                userRepository,
                careerRepository,
                userSkillRepository,
                skillGapService,
                dependencyService,
                recommendationService,
                promptBuilder,
                aiService,
                validator,
                new ObjectMapper()
        );

        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setFullName("Bob Builder");
        testUser.setTargetCareer("Frontend Developer");
        testUser.setExperienceLevel(ExperienceLevel.BEGINNER);
        testUser.setLearningStyle(LearningStyle.VISUAL);
        testUser.setDailyLearningHours(2);

        testCareer = new Career();
        testCareer.setId(careerId);
        testCareer.setTitle("Frontend Developer");
    }

    @Test
    void testGenerateLearningPath_GeminiSuccessAndValidatorPasses() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(careerRepository.findById(careerId)).thenReturn(Optional.of(testCareer));
        when(userSkillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        SkillGapItemResponse gap1 = new SkillGapItemResponse(UUID.randomUUID(), "HTML", "FrontEnd", "NONE", null, null, null, null, true, "gap");
        SkillGapAnalysisResponse gapResponse = new SkillGapAnalysisResponse(userId, "Bob", careerId, "Frontend Developer", 1, 0, 0, 1, 50.0, List.of(gap1));

        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapResponse);
        when(dependencyService.getLearningOrder(any()))
                .thenReturn(LearningOrderResponse.ok(List.of("HTML", "CSS"), List.of()));

        CourseRecommendationResponse rec = new CourseRecommendationResponse(
                1, courseId, "HTML Foundations", "Udemy", "http://udemy.com",
                CourseDifficulty.BEGINNER, CourseType.VIDEO_COURSE, BigDecimal.valueOf(4.8), BigDecimal.ZERO, true, 0.9, 0.95, 0.93,
                List.of("HTML"), List.of("HTML"), "High recommendation match"
        );
        RecommendationSummaryResponse recSummary = new RecommendationSummaryResponse(
                userId, "Bob Builder", careerId, "Frontend Developer", true, 1, List.of(rec)
        );

        when(recommendationService.getRecommendationsForUser(userId, careerId)).thenReturn(recSummary);
        when(validator.validateContext(any())).thenReturn(ValidationResult.ok());
        when(promptBuilder.buildPrompt(any())).thenReturn("Test Prompt");

        String geminiJson = String.format("""
                {
                  "summary": "Master Frontend Web Development step by step.",
                  "phases": [
                    {
                      "phaseNumber": 1,
                      "phaseTitle": "Phase 1: Web Fundamentals",
                      "targetSkills": ["HTML"],
                      "courseIds": ["%s"],
                      "estimatedDuration": "2 weeks",
                      "explanation": "Start with core HTML"
                    }
                  ]
                }
                """, courseId);

        when(aiService.executeOperation(any()))
                .thenReturn(AiResponse.ok(AiOperation.LEARNING_PATH, "gemini-1.5-flash", geminiJson));

        when(validator.validateResponse(any(), any())).thenReturn(ValidationResult.ok());

        PersonalizedLearningPathResponse response = learningPathService.generateLearningPath(userId, careerId);

        assertTrue(response.success());
        assertEquals("GEMINI", response.provider());
        assertEquals("gemini-1.5-flash", response.model());
        assertEquals(1, response.phases().size());
        assertEquals(courseId, response.phases().get(0).courses().get(0).courseId());
    }

    @Test
    void testGenerateLearningPath_ValidationFailsBothAttempts_TriggersRuleBasedFallback() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(careerRepository.findById(careerId)).thenReturn(Optional.of(testCareer));
        when(userSkillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        SkillGapItemResponse gap1 = new SkillGapItemResponse(UUID.randomUUID(), "HTML", "FrontEnd", "NONE", null, null, null, null, true, "gap");
        SkillGapAnalysisResponse gapResponse = new SkillGapAnalysisResponse(userId, "Bob", careerId, "Frontend Developer", 1, 0, 0, 1, 50.0, List.of(gap1));

        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapResponse);
        when(dependencyService.getLearningOrder(any()))
                .thenReturn(LearningOrderResponse.ok(List.of("HTML"), List.of()));

        CourseRecommendationResponse rec = new CourseRecommendationResponse(
                1, courseId, "Valid Course", "Udemy", "http://udemy.com",
                CourseDifficulty.BEGINNER, CourseType.VIDEO_COURSE, BigDecimal.valueOf(4.5), BigDecimal.ZERO, true, 0.8, 0.8, 0.8,
                List.of("HTML"), List.of("HTML"), "Match"
        );
        RecommendationSummaryResponse recSummary = new RecommendationSummaryResponse(
                userId, "Bob Builder", careerId, "Frontend Developer", true, 1, List.of(rec)
        );

        when(recommendationService.getRecommendationsForUser(userId, careerId)).thenReturn(recSummary);
        when(validator.validateContext(any())).thenReturn(ValidationResult.ok());
        when(promptBuilder.buildPrompt(any())).thenReturn("Test Prompt");

        String geminiJson = """
                {
                  "summary": "Bad path",
                  "phases": []
                }
                """;

        when(aiService.executeOperation(any()))
                .thenReturn(AiResponse.ok(AiOperation.LEARNING_PATH, "gemini-1.5-flash", geminiJson));

        when(validator.validateResponse(any(), any())).thenReturn(ValidationResult.invalid("Empty phases"));

        PersonalizedLearningPathResponse response = learningPathService.generateLearningPath(userId, careerId);

        assertTrue(response.success());
        assertEquals("FALLBACK_RULE_BASED", response.provider());
        verify(aiService, times(2)).executeOperation(any());
    }

    @Test
    void testGenerateLearningPath_RetrySucceeds_ReturnsGeminiPath() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(careerRepository.findById(careerId)).thenReturn(Optional.of(testCareer));
        when(userSkillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        SkillGapItemResponse gap1 = new SkillGapItemResponse(UUID.randomUUID(), "HTML", "FrontEnd", "NONE", null, null, null, null, true, "gap");
        SkillGapAnalysisResponse gapResponse = new SkillGapAnalysisResponse(userId, "Bob", careerId, "Frontend Developer", 1, 0, 0, 1, 50.0, List.of(gap1));

        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapResponse);
        when(dependencyService.getLearningOrder(any()))
                .thenReturn(LearningOrderResponse.ok(List.of("HTML"), List.of()));

        CourseRecommendationResponse rec = new CourseRecommendationResponse(
                1, courseId, "Valid Course", "Udemy", "http://udemy.com",
                CourseDifficulty.BEGINNER, CourseType.VIDEO_COURSE, BigDecimal.valueOf(4.5), BigDecimal.ZERO, true, 0.8, 0.8, 0.8,
                List.of("HTML"), List.of("HTML"), "Match"
        );
        RecommendationSummaryResponse recSummary = new RecommendationSummaryResponse(
                userId, "Bob Builder", careerId, "Frontend Developer", true, 1, List.of(rec)
        );

        when(recommendationService.getRecommendationsForUser(userId, careerId)).thenReturn(recSummary);
        when(validator.validateContext(any())).thenReturn(ValidationResult.ok());
        when(promptBuilder.buildPrompt(any())).thenReturn("Test Prompt");

        String geminiJson = String.format("""
                {
                  "summary": "Valid path",
                  "phases": [
                    {
                      "phaseNumber": 1,
                      "phaseTitle": "Phase 1",
                      "targetSkills": ["HTML"],
                      "courseIds": ["%s"],
                      "estimatedDuration": "2 weeks",
                      "explanation": "Valid course"
                    }
                  ]
                }
                """, courseId);

        when(aiService.executeOperation(any()))
                .thenReturn(AiResponse.ok(AiOperation.LEARNING_PATH, "gemini-1.5-flash", geminiJson));

        // First attempt fails, second attempt succeeds
        when(validator.validateResponse(any(), any()))
                .thenReturn(ValidationResult.invalid("Transient issue"))
                .thenReturn(ValidationResult.ok());

        PersonalizedLearningPathResponse response = learningPathService.generateLearningPath(userId, careerId);

        assertTrue(response.success());
        assertEquals("GEMINI", response.provider());
        verify(aiService, times(2)).executeOperation(any());
    }

    @Test
    void testGenerateRuleBasedFallback_CreatesDeterministicPath() {
        RecommendedCourseItem course = new RecommendedCourseItem(courseId, "Java Core", "Coursera", 0.88, "BEGINNER", List.of("Java"));
        LearningPathContext context = new LearningPathContext(
                userId,
                "Alice",
                "Java Backend Engineer",
                "BEGINNER",
                "AUDIO",
                1.0,
                List.of(),
                List.of("Java", "Spring Boot"),
                List.of("Java", "Spring Boot"),
                List.of(course)
        );

        PersonalizedLearningPathResponse response = learningPathService.generateRuleBasedFallback(context);

        assertTrue(response.success());
        assertEquals("FALLBACK_RULE_BASED", response.provider());
        assertFalse(response.phases().isEmpty());
    }
}
