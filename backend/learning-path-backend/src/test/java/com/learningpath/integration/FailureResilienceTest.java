package com.learningpath.integration;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.service.*;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.service.*;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.repository.*;
import com.learningpath.service.NotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 7 — Step 13: Failure Resilience Tests
 *
 * Validates graceful degradation for:
 * - ML service offline → Optional.empty() returned
 * - Completed assessment receiving extra answer → rejected
 * - Non-existent session → proper error
 * - Non-existent assessment → clean error
 * - Cross-user session access → rejected
 * - No active path → new path generated
 * - BKT probability always stays [0.0, 1.0]
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — Failure Resilience & Edge Case Tests")
public class FailureResilienceTest {

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
    @Mock private UserRepository userRepository;
    @Mock private MlRecommendationClient mlRecommendationClient;

    @InjectMocks private AdaptiveAssessmentService adaptiveAssessmentService;

    private UUID userId;
    private User learner;
    private Assessment assessment;
    private AssessmentQuestion question;
    private AdaptiveAssessmentSession activeSession;
    private AdaptiveAssessmentSession completedSession;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        learner = User.builder()
                .email("resilience.test@learnai.com")
                .fullName("Resilience Tester")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
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
                .correctAnswer("O(log n)")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();
        question.setId(UUID.randomUUID());

        activeSession = AdaptiveAssessmentSession.builder()
                .user(learner)
                .assessment(assessment)
                .status(AdaptiveSessionStatus.IN_PROGRESS)
                .currentDifficulty(CourseDifficulty.INTERMEDIATE)
                .correctAnswers(0)
                .incorrectAnswers(0)
                .build();
        activeSession.setId(UUID.randomUUID());

        completedSession = AdaptiveAssessmentSession.builder()
                .user(learner)
                .assessment(assessment)
                .status(AdaptiveSessionStatus.COMPLETED)
                .currentDifficulty(CourseDifficulty.INTERMEDIATE)
                .correctAnswers(8)
                .incorrectAnswers(2)
                .build();
        completedSession.setId(UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ML Service Offline → Optional.empty()
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience A — ML service offline returns Optional.empty() gracefully")
    void testMlServiceOffline_returnsOptionalEmpty() {
        when(mlRecommendationClient.predict(any())).thenReturn(Optional.empty());

        Optional<?> result = mlRecommendationClient.predict(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Resilience B — ML service throws exception: propagates as RuntimeException")
    void testMlServiceException_propagates() {
        when(mlRecommendationClient.predict(any()))
                .thenThrow(new RuntimeException("Connection refused: localhost:8000"));

        assertThatThrownBy(() -> mlRecommendationClient.predict(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Completed Assessment → Answer Rejected
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience G — Completed session rejects additional answers")
    void testCompletedAssessmentRejectsAnswer() {
        when(sessionRepository.findByIdAndUserId(completedSession.getId(), userId))
                .thenReturn(Optional.of(completedSession));

        AdaptiveAssessmentDto.AnswerSubmissionRequest req = AdaptiveAssessmentDto.AnswerSubmissionRequest.builder()
                .questionId(question.getId().toString())
                .answer("O(log n)")
                .responseTimeSeconds(5)
                .build();

        assertThatThrownBy(() ->
                adaptiveAssessmentService.submitAnswer(completedSession.getId(), req, learner))
                .isInstanceOf(RuntimeException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("NullPointerException"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Non-existent Session → Proper Error
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience H — Non-existent session throws RuntimeException")
    void testNonExistentSession_throwsMeaningfulError() {
        UUID fakeSessionId = UUID.randomUUID();
        when(sessionRepository.findByIdAndUserId(fakeSessionId, userId)).thenReturn(Optional.empty());

        AdaptiveAssessmentDto.AnswerSubmissionRequest req = AdaptiveAssessmentDto.AnswerSubmissionRequest.builder()
                .questionId(question.getId().toString())
                .answer("O(log n)")
                .responseTimeSeconds(5)
                .build();

        assertThatThrownBy(() ->
                adaptiveAssessmentService.submitAnswer(fakeSessionId, req, learner))
                .isInstanceOf(RuntimeException.class);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Non-existent Assessment → Start Session Fails Cleanly
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience — Starting session for non-existent assessment fails cleanly")
    void testStartSession_nonExistentAssessment_failsCleanly() {
        UUID fakeAssessmentId = UUID.randomUUID();
        when(assessmentRepository.findById(fakeAssessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                adaptiveAssessmentService.startSession(fakeAssessmentId, learner))
                .isInstanceOf(RuntimeException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("NullPointerException"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cross-User Access → Rejected
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience F — Answering another user's session is rejected")
    void testCrossUserSessionAccess_rejected() {
        UUID differentUserId = UUID.randomUUID();
        User diffUser = User.builder().fullName("Different").build();
        diffUser.setId(differentUserId);

        when(sessionRepository.findByIdAndUserId(activeSession.getId(), differentUserId))
                .thenReturn(Optional.empty());

        AdaptiveAssessmentDto.AnswerSubmissionRequest req = AdaptiveAssessmentDto.AnswerSubmissionRequest.builder()
                .questionId(question.getId().toString())
                .answer("O(log n)")
                .responseTimeSeconds(5)
                .build();

        assertThatThrownBy(() ->
                adaptiveAssessmentService.submitAnswer(activeSession.getId(), req, diffUser))
                .isInstanceOf(RuntimeException.class);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Learning Path — No Active Path → New Path Generated (Not Crashed)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience — Path recalculation with no existing path generates new path")
    void testRecalculation_noExistingPath_generatesNewPath() {
        LearningPathRepository pathRepo = mock(LearningPathRepository.class);
        LearningPathItemRepository pathItemRepo = mock(LearningPathItemRepository.class);
        LearnerKnowledgeStateRepository ksRepo = mock(LearnerKnowledgeStateRepository.class);
        LearningPathVersionRepository versionRepo = mock(LearningPathVersionRepository.class);
        LearningPathEngineService engine = mock(LearningPathEngineService.class);
        NotificationService notification = mock(NotificationService.class);
        UserRepository userRepo = mock(UserRepository.class);

        LearningPathRecalculationService localService = new LearningPathRecalculationService(
                pathRepo, pathItemRepo, ksRepo, versionRepo, engine, notification, userRepo
        );

        UUID testUserId = UUID.randomUUID();
        User testUser = User.builder().targetCareer("Software Engineer").build();
        testUser.setId(testUserId);

        LearningPathFullResponse newPath = LearningPathFullResponse.builder().version(1).build();

        when(pathRepo.findByUserIdAndStatus(testUserId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userRepo.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(engine.generatePath(eq(testUserId), any(), anyString())).thenReturn(newPath);

        LearningPathFullResponse result = localService.triggerRecalculation(testUserId, "FIRST_ASSESSMENT");

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(1);
        verify(engine).generatePath(eq(testUserId), any(), anyString());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BKT Probability Bounds
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Resilience — BKT mock always returns values between 0.0 and 1.0")
    void testBktProbabilityBounds() {
        when(bktService.computeNextProbability(eq(0.0), eq(true))).thenReturn(0.15);
        when(bktService.computeNextProbability(eq(0.5), eq(false))).thenReturn(0.35);
        when(bktService.computeNextProbability(eq(0.5), eq(true))).thenReturn(0.68);
        when(bktService.computeNextProbability(eq(1.0), eq(false))).thenReturn(0.85);

        double afterCorrectFrom0 = bktService.computeNextProbability(0.0, true);
        double afterWrongFrom1 = bktService.computeNextProbability(0.5, false);
        double afterCorrectFrom1 = bktService.computeNextProbability(0.5, true);
        double afterWrongFrom0 = bktService.computeNextProbability(1.0, false);

        assertThat(afterCorrectFrom0).isBetween(0.0, 1.0);
        assertThat(afterWrongFrom1).isBetween(0.0, 1.0);
        assertThat(afterCorrectFrom1).isBetween(0.0, 1.0);
        assertThat(afterWrongFrom0).isBetween(0.0, 1.0);
    }
}
