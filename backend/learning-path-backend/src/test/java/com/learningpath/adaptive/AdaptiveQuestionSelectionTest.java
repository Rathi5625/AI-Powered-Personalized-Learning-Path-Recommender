package com.learningpath.adaptive;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.service.AdaptiveAssessmentService;
import com.learningpath.adaptive.service.AdaptiveDifficultyService;
import com.learningpath.adaptive.service.BayesianKnowledgeTracingService;
import com.learningpath.adaptive.service.LearnerBehaviorService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.AdaptiveSessionStatus;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.QuestionType;
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
public class AdaptiveQuestionSelectionTest {

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

    @InjectMocks
    private AdaptiveAssessmentService adaptiveAssessmentService;

    private UUID userId;
    private User user;
    private Assessment assessment;
    private UUID sessionId;
    private AdaptiveAssessmentSession session;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().fullName("Alice Learner").build();
        user.setId(userId);

        Skill skill = Skill.builder().name("Binary Search").build();
        skill.setId(UUID.randomUUID());

        assessment = Assessment.builder().title("Binary Search Assessment").skill(skill).build();
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
    void testGetNextQuestion_SelectsCandidateMatchingCurrentDifficulty() {
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId)).thenReturn(List.of());

        AssessmentQuestion q1 = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("What is the time complexity of Binary Search?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .optionsJson("[\"O(n)\", \"O(log n)\", \"O(1)\", \"O(n log n)\"]")
                .correctAnswer("O(log n)")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();
        q1.setId(UUID.randomUUID());

        AssessmentQuestion q2 = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("Find the peak element in an array.")
                .questionType(QuestionType.SINGLE_CHOICE)
                .optionsJson("[\"A\", \"B\", \"C\", \"D\"]")
                .correctAnswer("A")
                .difficulty(CourseDifficulty.ADVANCED)
                .build();
        q2.setId(UUID.randomUUID());

        when(questionRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(q1, q2));

        AdaptiveAssessmentDto.NextQuestionResponse resp = adaptiveAssessmentService.getNextQuestion(sessionId, user);

        assertNotNull(resp);
        assertFalse(resp.isTerminated());
        assertEquals(q1.getId().toString(), resp.getQuestionId());
        assertEquals(CourseDifficulty.INTERMEDIATE, resp.getDifficulty());
        assertEquals(1, resp.getQuestionNumber());
    }

    @Test
    void testGetNextQuestion_FallsBackToClosestDifficulty_WhenExactUnavailable() {
        // Session is at ADVANCED difficulty
        session.setCurrentDifficulty(CourseDifficulty.ADVANCED);
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId)).thenReturn(List.of());

        // Pool only has BEGINNER and INTERMEDIATE (ADVANCED is missing)
        AssessmentQuestion qBeginner = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("Binary search basic concept?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .optionsJson("[\"A\", \"B\"]")
                .correctAnswer("A")
                .difficulty(CourseDifficulty.BEGINNER)
                .build();
        qBeginner.setId(UUID.randomUUID());

        AssessmentQuestion qIntermediate = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("Binary search on sorted array?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .optionsJson("[\"A\", \"B\"]")
                .correctAnswer("A")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();
        qIntermediate.setId(UUID.randomUUID());

        when(questionRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(qBeginner, qIntermediate));

        AdaptiveAssessmentDto.NextQuestionResponse resp = adaptiveAssessmentService.getNextQuestion(sessionId, user);

        assertNotNull(resp);
        assertFalse(resp.isTerminated());
        // For ADVANCED, fallback hierarchy selects INTERMEDIATE before BEGINNER
        assertEquals(qIntermediate.getId().toString(), resp.getQuestionId());
        assertEquals(CourseDifficulty.INTERMEDIATE, resp.getDifficulty());
    }

    @Test
    void testGetNextQuestion_TerminatesGracefully_WhenAllQuestionsAnswered() {
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        AssessmentQuestion q1 = AssessmentQuestion.builder()
                .assessment(assessment)
                .questionText("Question 1")
                .questionType(QuestionType.SINGLE_CHOICE)
                .difficulty(CourseDifficulty.BEGINNER)
                .build();
        q1.setId(UUID.randomUUID());

        AdaptiveAssessmentResponse r1 = AdaptiveAssessmentResponse.builder()
                .session(session)
                .question(q1)
                .attemptNumber(1)
                .build();

        when(responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId)).thenReturn(List.of(r1));
        when(questionRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(q1));

        AdaptiveAssessmentDto.NextQuestionResponse resp = adaptiveAssessmentService.getNextQuestion(sessionId, user);

        assertNotNull(resp);
        assertTrue(resp.isTerminated());
        assertEquals(AdaptiveSessionStatus.COMPLETED, session.getStatus());
        assertTrue(session.getTerminationReason().contains("pool completed"));
    }
}
