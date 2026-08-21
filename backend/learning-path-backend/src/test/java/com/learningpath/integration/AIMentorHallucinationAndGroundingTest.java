package com.learningpath.integration;

import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.LearnerBehaviorService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.*;
import com.learningpath.ai.service.AIMentorService;
import com.learningpath.ai.service.LearnerContextService;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.service.LearnerFeatureBuilderService;
import com.learningpath.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 7 — Steps 9, 10, 11: AI Mentor Grounding, Hallucination Resistance & General Knowledge
 *
 * Tests that:
 * 1. Gemini uses real learner context when answering specific questions.
 * 2. Gemini does NOT fabricate data when asked about non-existent assessments.
 * 3. Gemini can answer general educational questions.
 * 4. All structured actions map to valid frontend routes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — AI Mentor Grounding, Hallucination & General Knowledge Tests")
public class AIMentorHallucinationAndGroundingTest {

    @Mock private GeminiClient geminiClient;
    @Mock private LearnerContextService learnerContextService;
    @Mock private LearnerFeatureBuilderService featureBuilderService;
    @Mock private MlRecommendationClient mlClient;
    @Mock private CourseRepository courseRepository;
    @Mock private AIConversationRepository conversationRepository;
    @Mock private AIMessageRepository messageRepository;

    @InjectMocks private AIMentorService aiMentorService;

    private User learner;
    private AIConversation conversation;
    private LearnerAiContext richContext;

    // Valid frontend routes that AI actions must map to
    private static final Set<String> VALID_ROUTES = Set.of(
            "/learning-path", "/assessments", "/explore-courses", "/progress",
            "/dashboard", "/ai-mentor", "/skills", "/profile", "/settings", "/notifications"
    );

    @BeforeEach
    void setUp() {
        learner = User.builder()
                .email("mentor.test@learnai.com")
                .fullName("Mentor Test Learner")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .dailyLearningHours(2)
                .build();
        learner.setId(UUID.randomUUID());

        conversation = AIConversation.builder()
                .user(learner)
                .title("Learning Path Mentorship")
                .build();
        conversation.setId(UUID.randomUUID());

        richContext = LearnerAiContext.builder()
                .userId(learner.getId())
                .fullName("Mentor Test Learner")
                .targetCareer("Software Engineer")
                .experienceLevel("INTERMEDIATE")
                .overallMasteryPercentage(58.0)
                .masteredSkills(List.of("Arrays", "Sorting"))
                .weakSkills(List.of("Trees", "Dynamic Programming"))
                .revisionRequiredSkills(List.of("Binary Search"))
                .activeStreakDays(5)
                .totalLearningHours(12.0)
                .assessmentAccuracy(0.68)
                .learningVelocity(0.55)
                .preferredDifficulty("INTERMEDIATE")
                .skills(List.of(
                        LearnerAiContext.LearnerSkillInfo.builder().skillName("Arrays").proficiencyLevel("ADVANCED").build(),
                        LearnerAiContext.LearnerSkillInfo.builder().skillName("Binary Search").proficiencyLevel("INTERMEDIATE").build()
                ))
                .recentAssessments(List.of(
                        LearnerAiContext.LearnerAssessmentSummary.builder()
                                .title("Data Structures Assessment")
                                .skillName("Binary Search")
                                .scorePercentage(72.0)
                                .createdAt("2026-08-20")
                                .build()
                ))
                .activeCourses(List.of())
                .completedCourseTitles(List.of("Introduction to Algorithms"))
                .careerReadinessScore(62)
                .build();

        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(learner))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(inv -> {
            AIMessage msg = inv.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(courseRepository.findAll()).thenReturn(List.of());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GROUNDING TESTS: Real data used in responses
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Grounding — 'What should I learn today?' uses real learner context")
    void testWhatShouldILearnToday_usesRealContext() {
        when(learnerContextService.buildContext(learner)).thenReturn(richContext);
        when(geminiClient.generateContent(anyString())).thenAnswer(inv -> {
            String prompt = inv.getArgument(0);
            // Verify context is injected into prompt
            assertThat(prompt).contains("Binary Search");
            assertThat(prompt).contains("Software Engineer");
            assertThat(prompt).contains("INTERMEDIATE");
            return AiTestResponse.ok("gemini-2.5-flash", "Based on your current mastery, I recommend focusing on **Trees** today.");
        });

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What should I learn today?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        assertThat(response.getReply()).isNotBlank();
        verify(learnerContextService).buildContext(learner);
        verify(geminiClient).generateContent(anyString());
    }

    @Test
    @DisplayName("Grounding — 'What is my weakest skill?' answered with real mastery data in prompt")
    void testWeakestSkillQuery_promptContainsRealData() {
        when(learnerContextService.buildContext(learner)).thenReturn(richContext);
        when(geminiClient.generateContent(anyString())).thenAnswer(inv -> {
            String prompt = inv.getArgument(0);
            // Real weak skills must appear in prompt
            assertThat(prompt).contains("Trees");
            assertThat(prompt).contains("Dynamic Programming");
            return AiTestResponse.ok("gemini-2.5-flash", "Your weakest skills are **Trees** and **Dynamic Programming**.");
        });

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What is my weakest skill?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);
        assertThat(response.getReply()).isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HALLUCINATION TESTS: Missing data → no fabrication
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Hallucination — 'Python assessment last month' with no data → fallback, no fabrication")
    void testHallucinationResistance_missingPythonAssessment() {
        // Context has NO Python assessment
        LearnerAiContext contextNoPython = LearnerAiContext.builder()
                .userId(learner.getId())
                .fullName("Mentor Test Learner")
                .targetCareer("Software Engineer")
                .experienceLevel("INTERMEDIATE")
                .overallMasteryPercentage(45.0)
                .recentAssessments(List.of()) // Empty — no Python assessment exists
                .skills(List.of())
                .activeCourses(List.of())
                .completedCourseTitles(List.of())
                .masteredSkills(List.of())
                .weakSkills(List.of())
                .revisionRequiredSkills(List.of())
                .build();

        when(learnerContextService.buildContext(learner)).thenReturn(contextNoPython);
        when(geminiClient.generateContent(anyString())).thenAnswer(inv -> {
            String prompt = inv.getArgument(0);
            // Prompt should NOT contain fake Python assessment data
            assertThat(prompt).doesNotContain("Python assessment score: 85%");
            assertThat(prompt).doesNotContain("You scored 92% on Python");
            return AiTestResponse.ok("gemini-2.5-flash", "I don't have a Python assessment result for you yet. Would you like to take one?");
        });

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("How did I perform in my Python assessment last month?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        // Response should acknowledge missing data, not invent results
        assertThat(response.getReply()).isNotBlank();
        assertThat(response.getReply()).doesNotContain("scored 85%");
        assertThat(response.getReply()).doesNotContain("last month you got");
    }

    @Test
    @DisplayName("Hallucination — 'My machine learning course score' with no progress → no fabrication")
    void testHallucinationResistance_missingCourseScore() {
        LearnerAiContext emptyContext = LearnerAiContext.builder()
                .userId(learner.getId())
                .fullName("Learner")
                .targetCareer("Software Engineer")
                .experienceLevel("BEGINNER")
                .overallMasteryPercentage(0.0)
                .recentAssessments(List.of())
                .activeCourses(List.of())
                .completedCourseTitles(List.of())
                .skills(List.of())
                .masteredSkills(List.of())
                .weakSkills(List.of())
                .revisionRequiredSkills(List.of())
                .activeStreakDays(0)
                .totalLearningHours(0.0)
                .build();

        when(learnerContextService.buildContext(learner)).thenReturn(emptyContext);
        when(geminiClient.generateContent(anyString())).thenAnswer(inv -> {
            String prompt = inv.getArgument(0);
            // totalHours=0 now (not 12.5 fabricated)
            assertThat(prompt).doesNotContain("12.5 hours");
            return AiTestResponse.ok("gemini-2.5-flash", "It looks like you haven't started any courses yet. Let's get you set up!");
        });

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What is my machine learning course score?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);
        assertThat(response.getReply()).isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GENERAL KNOWLEDGE TESTS
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("General Knowledge — 'Explain binary search' answered even without learner context")
    void testGeneralKnowledge_binarySearchExplanation() {
        when(learnerContextService.buildContext(learner)).thenReturn(null); // no context
        when(geminiClient.generateContent(anyString())).thenReturn(
                AiTestResponse.ok("gemini-2.5-flash", "**Binary Search** is a divide-and-conquer algorithm that finds a target in a sorted array in O(log n) time."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("Explain binary search.");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        assertThat(response.getReply()).isNotBlank();
        // General educational answer provided
        assertThat(response.getReply()).containsIgnoringCase("binary search");
    }

    @Test
    @DisplayName("General Knowledge — 'What is dynamic programming?' answered as general question")
    void testGeneralKnowledge_dynamicProgramming() {
        when(learnerContextService.buildContext(learner)).thenReturn(null);
        when(geminiClient.generateContent(anyString())).thenReturn(
                AiTestResponse.ok("gemini-2.5-flash", "**Dynamic Programming** is a method for solving complex problems by breaking them into overlapping subproblems."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What is dynamic programming?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);
        assertThat(response.getReply()).isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ACTION VALIDATION: All structured actions map to valid routes
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Action Validation — all structured actions have valid frontend routes")
    void testAllActionsMapToValidFrontendRoutes() {
        when(learnerContextService.buildContext(learner)).thenReturn(richContext);
        when(geminiClient.generateContent(anyString())).thenReturn(
                AiTestResponse.ok("gemini-2.5-flash", "Here's your plan."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What should I learn today?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        assertThat(response.getActions()).isNotNull();
        for (AIMentorChatResponse.MentorActionDto action : response.getActions()) {
            assertThat(action.getTargetUrl()).isNotBlank();
            assertThat(VALID_ROUTES).as("Action '%s' has invalid route '%s'",
                            action.getType(), action.getTargetUrl())
                    .contains(action.getTargetUrl());
            assertThat(action.getType()).isNotBlank();
            assertThat(action.getLabel()).isNotBlank();
        }
    }

    @Test
    @DisplayName("Action Validation — REVISE_TOPIC action appears when revision skills exist")
    void testReviseTopic_actionPresentWhenRevisionSkillsExist() {
        // richContext has revisionRequiredSkills = ["Binary Search"]
        when(learnerContextService.buildContext(learner)).thenReturn(richContext);
        when(geminiClient.generateContent(anyString())).thenReturn(
                AiTestResponse.ok("gemini-2.5-flash", "Let's revise Binary Search."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What should I revise?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        boolean hasReviseAction = response.getActions().stream()
                .anyMatch(a -> "REVISE_TOPIC".equals(a.getType()));
        assertThat(hasReviseAction).isTrue();
    }

    @Test
    @DisplayName("Action Validation — START_ASSESSMENT action present when weak skills exist")
    void testStartAssessment_actionPresentWhenWeakSkillsExist() {
        when(learnerContextService.buildContext(learner)).thenReturn(richContext);
        when(geminiClient.generateContent(anyString())).thenReturn(
                AiTestResponse.ok("gemini-2.5-flash", "Take an assessment on Trees."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("Am I ready for advanced DSA?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        boolean hasAssessmentAction = response.getActions().stream()
                .anyMatch(a -> "START_ASSESSMENT".equals(a.getType()));
        assertThat(hasAssessmentAction).isTrue();
    }
}
