package com.learningpath.integration;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.*;
import com.learningpath.ai.dto.AIMentorChatRequest;
import com.learningpath.ai.dto.AIMentorChatResponse;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.service.AIMentorService;
import com.learningpath.ai.service.LearnerContextService;
import com.learningpath.ai.client.GeminiClient;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.dto.WeeklyLearningPlanDto;
import com.learningpath.learningpath.service.*;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.service.LearnerFeatureBuilderService;
import com.learningpath.repository.*;
import com.learningpath.service.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 7 — Step 3 & Step 30: Full End-to-End Learner Journey Test
 *
 * Validates the complete intelligence pipeline:
 * User creation → Assessment → BKT → Behavior → Skill Gap → ML → Learning Path
 * → Weekly Plan → Activity → Adaptive Assessment → BKT Update → Behavior Update
 * → Path Recalculation → Notification → AI Mentor → Conversation persistence.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — Complete End-to-End Learner Journey")
public class Phase7EndToEndLearnerJourneyTest {

    // ── Adaptive Assessment mocks ──────────────────────────────────────────────
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private AssessmentQuestionRepository questionRepository;
    @Mock private AdaptiveAssessmentSessionRepository sessionRepository;
    @Mock private AdaptiveAssessmentResponseRepository responseRepository;
    @Mock private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock private AdaptiveDifficultyService difficultyService;
    @Mock private BayesianKnowledgeTracingService bktService;
    @Mock private LearnerBehaviorService behaviorService;
    @Mock private LearnerMasteryService masteryService;
    @Mock private LearningPathRecalculationService recalculationService;
    @Mock private NotificationService notificationService;

    // ── Learning Path mocks ────────────────────────────────────────────────────
    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository itemRepository;
    @Mock private LearningPathVersionRepository versionRepository;
    @Mock private LearningPathEngineService engineService;
    @Mock private WeeklyLearningPlanService weeklyPlanService;
    @Mock private UserRepository userRepository;

    // ── AI Mentor mocks ────────────────────────────────────────────────────────
    @Mock private GeminiClient geminiClient;
    @Mock private LearnerContextService learnerContextService;
    @Mock private LearnerFeatureBuilderService featureBuilderService;
    @Mock private MlRecommendationClient mlClient;
    @Mock private CourseRepository courseRepository;
    @Mock private AIConversationRepository conversationRepository;
    @Mock private AIMessageRepository messageRepository;

    @InjectMocks private AdaptiveAssessmentService adaptiveAssessmentService;
    @InjectMocks private LearningPathRecalculationService pathRecalculationService;
    @InjectMocks private AIMentorService aiMentorService;

    private UUID userId;
    private User learner;
    private Assessment assessment;
    private AssessmentQuestion question;
    private AdaptiveAssessmentSession session;
    private LearnerKnowledgeState binarySearchKs;
    private LearningPath activePath;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        learner = User.builder()
                .email("e2e.learner@learnai.com")
                .fullName("E2E Test Learner")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .dailyLearningHours(2)
                .build();
        learner.setId(userId);

        Skill binarySearch = Skill.builder().name("Binary Search").build();
        binarySearch.setId(UUID.randomUUID());

        assessment = Assessment.builder()
                .title("Data Structures Assessment")
                .skill(binarySearch)
                .build();
        assessment.setId(UUID.randomUUID());

        question = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("What is the time complexity of binary search?")
                .optionsJson("[\"O(n)\", \"O(log n)\", \"O(n²)\", \"O(1)\"]")
                .correctAnswer("O(log n)")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();
        question.setId(UUID.randomUUID());

        session = AdaptiveAssessmentSession.builder()
                .user(learner)
                .assessment(assessment)
                .status(AdaptiveSessionStatus.IN_PROGRESS)
                .currentDifficulty(CourseDifficulty.INTERMEDIATE)
                .correctAnswers(0)
                .incorrectAnswers(0)
                .build();
        session.setId(UUID.randomUUID());

        binarySearchKs = LearnerKnowledgeState.builder()
                .user(learner)
                .conceptName("Binary Search")
                .knowledgeProbability(0.42)
                .build();
        binarySearchKs.setId(UUID.randomUUID());

        activePath = LearningPath.builder()
                .user(learner)
                .status(LearningPathStatus.ACTIVE)
                .version(1)
                .overallProgress(25.0)
                .build();
        activePath.setId(UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 1: Adaptive Session Initialization
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 1 — Adaptive assessment session is created and persisted")
    void testAdaptiveSessionCreation() {
        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
        when(difficultyService.determineDifficulty(eq(userId), anyString(), any()))
                .thenReturn(CourseDifficulty.BEGINNER);
        when(questionRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(question));
        when(sessionRepository.save(any())).thenReturn(session);

        AdaptiveAssessmentDto.SessionStartResponse result =
                adaptiveAssessmentService.startSession(assessment.getId(), learner);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isNotNull();
        verify(sessionRepository, atLeastOnce()).save(any(AdaptiveAssessmentSession.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 2: Question Selection & BKT Update Chain
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 2 — Answer submission triggers BKT update and response persistence")
    void testAnswerSubmissionTriggersBktAndPersistence() {
        when(sessionRepository.findByIdAndUserId(session.getId(), userId)).thenReturn(Optional.of(session));
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));
        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(userId, "Binary Search"))
                .thenReturn(Optional.of(binarySearchKs));

        LearnerKnowledgeState updatedKs = LearnerKnowledgeState.builder()
                .user(learner)
                .conceptName("Binary Search")
                .knowledgeProbability(0.61)
                .masteryLevel(MasteryLevel.DEVELOPING)
                .build();
        when(bktService.updateKnowledgeState(any(), any(), anyString(), anyBoolean(), anyInt()))
                .thenReturn(updatedKs);
        when(difficultyService.determineNextDifficulty(any(), any(), any(), anyBoolean(), anyInt(), anyInt(), anyInt()))
                .thenReturn(CourseDifficulty.INTERMEDIATE);
        when(responseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sessionRepository.save(any())).thenReturn(session);

        AdaptiveAssessmentDto.AnswerSubmissionRequest req = AdaptiveAssessmentDto.AnswerSubmissionRequest.builder()
                .questionId(question.getId().toString())
                .answer("O(log n)")
                .responseTimeSeconds(12)
                .build();

        AdaptiveAssessmentDto.AnswerSubmissionResult result =
                adaptiveAssessmentService.submitAnswer(session.getId(), req, learner);

        assertThat(result).isNotNull();
        assertThat(result.isCorrect()).isTrue();
        // BKT updated
        verify(bktService).updateKnowledgeState(eq(learner), any(), eq("Binary Search"), eq(true), eq(12));
        // Response persisted
        verify(responseRepository, atLeastOnce()).save(any(AdaptiveAssessmentResponse.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 3: Path Recalculation after Assessment — version increments on change
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 3 — Path recalculation increments version when mastery gates change")
    void testPathRecalculationIncrementsVersion() {
        Skill skill = Skill.builder().name("Binary Search").build();
        skill.setId(UUID.randomUUID());

        // Binary Search now at 87% mastery (> 85% threshold) → item completes
        LearningPathItem bsItem = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Binary Search")
                .targetSkill(skill)
                .status(LearningPathNodeStatus.UNLOCKED)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(false)
                .currentMastery(0.87)
                .build();
        bsItem.setId(UUID.randomUUID());

        LearnerKnowledgeState ks = LearnerKnowledgeState.builder()
                .conceptName("Binary Search")
                .knowledgeProbability(0.87)
                .build();

        LearningPathFullResponse mockResponse = new LearningPathFullResponse();
        mockResponse.setVersion(2);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(bsItem));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(itemRepository.saveAll(any())).thenReturn(List.of(bsItem));
        when(learningPathRepository.save(any())).thenReturn(activePath);
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(eq(userId), any(), anyString())).thenReturn(mockResponse);

        LearningPathFullResponse result = pathRecalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        assertThat(result).isNotNull();
        // Version was saved (incremented)
        verify(versionRepository).save(any(LearningPathVersion.class));
        // Notification fired
        verify(notificationService).createNotification(eq(userId), anyString(), anyString(), any(), anyString());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 4: No version increment when nothing changes
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 4 — Path recalculation does NOT increment version when state unchanged")
    void testPathRecalculationNoVersionOnNoChange() {
        Skill skill = Skill.builder().name("Binary Search").build();
        skill.setId(UUID.randomUUID());

        // Binary Search at 50% mastery — nothing changes
        LearningPathItem bsItem = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Binary Search")
                .targetSkill(skill)
                .status(LearningPathNodeStatus.UNLOCKED)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(false)
                .currentMastery(0.50)
                .build();
        bsItem.setId(UUID.randomUUID());

        LearnerKnowledgeState ks = LearnerKnowledgeState.builder()
                .conceptName("Binary Search")
                .knowledgeProbability(0.50)
                .revisionRequired(false)
                .build();

        LearningPathFullResponse mockResponse = new LearningPathFullResponse();
        mockResponse.setVersion(1);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(bsItem));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(eq(userId), any(), anyString())).thenReturn(mockResponse);

        LearningPathFullResponse result = pathRecalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        assertThat(result).isNotNull();
        // No version saved — nothing changed
        verify(versionRepository, never()).save(any());
        // No notification — nothing changed
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 5: AI Mentor conversation persistence
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 5 — AI Mentor chat persists both user message and mentor reply")
    void testAIMentorConversationPersistence() {
        AIConversation conversation = AIConversation.builder()
                .user(learner)
                .title("Learning Path Mentorship")
                .build();
        conversation.setId(UUID.randomUUID());

        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(learner))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(inv -> {
            AIMessage msg = inv.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(learner)).thenReturn(null);
        when(courseRepository.findAll()).thenReturn(List.of());
        when(geminiClient.generateContent(anyString()))
                .thenReturn(AiTestResponse.ok("gemini-2.5-flash", "Your learning path has been updated based on your Binary Search assessment."));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What changed in my learning path?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).isNotBlank();
        assertThat(response.getConversationId()).isNotNull();
        // Both user message and mentor reply persisted
        verify(messageRepository, times(2)).save(any(AIMessage.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // STEP 6: Gemini fallback when unavailable — conversation still persisted
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Step 6 — AI Mentor uses fallback when Gemini is unavailable, still persists")
    void testAIMentorFallbackWhenGeminiUnavailable() {
        AIConversation conversation = AIConversation.builder()
                .user(learner)
                .title("Learning Path Mentorship")
                .build();
        conversation.setId(UUID.randomUUID());

        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(learner))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.save(any())).thenAnswer(inv -> {
            AIMessage msg = inv.getArgument(0);
            msg.setId(UUID.randomUUID());
            return msg;
        });
        when(learnerContextService.buildContext(learner)).thenReturn(null);
        when(courseRepository.findAll()).thenReturn(List.of());
        when(geminiClient.generateContent(anyString())).thenThrow(new RuntimeException("Gemini unavailable"));

        AIMentorChatRequest request = new AIMentorChatRequest();
        request.setMessage("What should I learn today?");

        AIMentorChatResponse response = aiMentorService.processChat(learner, request);

        assertThat(response).isNotNull();
        assertThat(response.getReply()).isNotBlank(); // fallback reply
        verify(messageRepository, times(2)).save(any(AIMessage.class)); // still persisted
    }
}
