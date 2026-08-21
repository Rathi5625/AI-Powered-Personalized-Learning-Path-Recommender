package com.learningpath.adaptive;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.service.*;
import com.learningpath.config.BktConfig;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.service.LearningPathRecalculationService;
import com.learningpath.repository.*;
import com.learningpath.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DifficultyAdjustmentAndResponseTimeTest {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentQuestionRepository questionRepository;
    @Mock
    private AdaptiveAssessmentSessionRepository sessionRepository;
    @Mock
    private AdaptiveAssessmentResponseRepository responseRepository;
    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock
    private AdaptiveDifficultyService difficultyService;
    @Mock
    private BayesianKnowledgeTracingService bktService;
    @Mock
    private LearnerBehaviorService behaviorService;
    @Mock
    private LearnerMasteryService masteryService;
    @Mock
    private LearningPathRecalculationService recalculationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private AssessmentResultRepository assessmentResultRepository;

    @InjectMocks
    private AdaptiveAssessmentService assessmentService;

    private UUID userId;
    private User user;
    private Assessment assessment;
    private UUID sessionId;
    private AdaptiveAssessmentSession session;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().fullName("Bob Learner").build();
        user.setId(userId);

        Skill skill = Skill.builder().name("Trees").build();
        skill.setId(UUID.randomUUID());

        assessment = Assessment.builder().title("Tree Traversals").skill(skill).build();
        assessment.setId(UUID.randomUUID());

        sessionId = UUID.randomUUID();
        session = AdaptiveAssessmentSession.builder()
                .user(user)
                .assessment(assessment)
                .status(AdaptiveSessionStatus.IN_PROGRESS)
                .currentDifficulty(CourseDifficulty.INTERMEDIATE)
                .questionsAsked(0)
                .correctAnswers(0)
                .build();
        session.setId(sessionId);
    }

    @Test
    void testSubmitAnswer_DetectsPossibleGuessOnFastAdvancedQuestion() {
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        UUID questionId = UUID.randomUUID();
        AssessmentQuestion question = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("Perform iterative postorder traversal")
                .correctAnswer("Stack based")
                .difficulty(CourseDifficulty.ADVANCED)
                .build();
        question.setId(questionId);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        // Prior knowledge was low (0.30)
        LearnerKnowledgeState priorState = LearnerKnowledgeState.builder()
                .conceptName("Trees")
                .knowledgeProbability(0.30)
                .build();
        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(userId, "Trees"))
                .thenReturn(Optional.of(priorState));

        LearnerKnowledgeState updatedState = LearnerKnowledgeState.builder()
                .conceptName("Trees")
                .knowledgeProbability(0.55)
                .masteryLevel(MasteryLevel.DEVELOPING)
                .consecutiveCorrect(1)
                .consecutiveIncorrect(0)
                .build();
        when(bktService.updateKnowledgeState(any(), any(), any(), any(Boolean.class), any(Integer.class)))
                .thenReturn(updatedState);

        when(difficultyService.determineNextDifficulty(any(), any(), any(), any(Boolean.class), any(Integer.class), any(Integer.class), any(Integer.class)))
                .thenReturn(CourseDifficulty.ADVANCED);

        // Fast response (2 seconds)
        AdaptiveAssessmentDto.AnswerSubmissionRequest req = AdaptiveAssessmentDto.AnswerSubmissionRequest.builder()
                .questionId(questionId.toString())
                .answer("Stack based")
                .responseTimeSeconds(2)
                .build();

        AdaptiveAssessmentDto.AnswerSubmissionResult result = assessmentService.submitAnswer(sessionId, req, user);

        assertNotNull(result);
        assertTrue(result.isCorrect());
        assertTrue(result.isPossibleGuess(), "Fast correct answer on advanced question with low prior knowledge should be flagged as possible guess");
    }

    @Test
    void testDifficultyService_SmoothedTransitions() {
        AdaptiveDifficultyService diffService = new AdaptiveDifficultyService(knowledgeStateRepository);

        // Step up: Beginner -> Intermediate
        CourseDifficulty next1 = diffService.determineNextDifficulty(
                userId, "Trees", CourseDifficulty.BEGINNER, true, 10, 2, 0);
        assertEquals(CourseDifficulty.INTERMEDIATE, next1);

        // Step up: Intermediate -> Advanced
        CourseDifficulty next2 = diffService.determineNextDifficulty(
                userId, "Trees", CourseDifficulty.INTERMEDIATE, true, 10, 2, 0);
        assertEquals(CourseDifficulty.ADVANCED, next2);

        // Step down: Advanced -> Intermediate on failure
        CourseDifficulty next3 = diffService.determineNextDifficulty(
                userId, "Trees", CourseDifficulty.ADVANCED, false, 30, 0, 2);
        assertEquals(CourseDifficulty.INTERMEDIATE, next3);

        // Step down: Intermediate -> Beginner on repeated failure
        CourseDifficulty next4 = diffService.determineNextDifficulty(
                userId, "Trees", CourseDifficulty.INTERMEDIATE, false, 30, 0, 2);
        assertEquals(CourseDifficulty.BEGINNER, next4);
    }
}
