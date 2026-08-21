package com.learningpath.adaptive;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdaptiveAssessmentSessionAndStoppingTest {

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
    private AdaptiveAssessmentService assessmentService;

    private UUID userId;
    private User user;
    private Assessment assessment;
    private UUID sessionId;
    private AdaptiveAssessmentSession session;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().fullName("Charlie Learner").build();
        user.setId(userId);

        Skill skill = Skill.builder().name("Dynamic Programming").build();
        skill.setId(UUID.randomUUID());

        assessment = Assessment.builder().title("DP Mastery").skill(skill).build();
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
    void testStartSession_InitializesSessionWithCalculatedDifficulty() {
        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
        when(difficultyService.determineDifficulty(userId, "Dynamic Programming", CourseDifficulty.BEGINNER))
                .thenReturn(CourseDifficulty.INTERMEDIATE);

        AssessmentQuestion q1 = AssessmentQuestion.builder().assessment(assessment).questionText("DP memoization").build();
        when(questionRepository.findAllByAssessmentId(assessment.getId())).thenReturn(List.of(q1));

        when(sessionRepository.save(any(AdaptiveAssessmentSession.class))).thenAnswer(inv -> {
            AdaptiveAssessmentSession s = inv.getArgument(0);
            s.setId(sessionId);
            return s;
        });

        AdaptiveAssessmentDto.SessionStartResponse resp = assessmentService.startSession(assessment.getId(), user);

        assertNotNull(resp);
        assertEquals(sessionId, resp.getSessionId());
        assertEquals(CourseDifficulty.INTERMEDIATE, resp.getCurrentDifficulty());
        assertEquals(AdaptiveSessionStatus.IN_PROGRESS, resp.getStatus());
    }

    @Test
    void testGetSessionResult_ReturnsRichGroundedResult() {
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        session.setQuestionsAsked(6);
        session.setCorrectAnswers(5);
        session.setCurrentAbilityEstimate(0.82);
        session.setConfidenceScore(0.88);
        session.setConfidenceLevel(ConfidenceLevel.HIGH);
        session.setCurrentDifficulty(CourseDifficulty.ADVANCED);
        session.setAverageResponseTimeSeconds(24.5);

        AdaptiveAssessmentResponse r1 = AdaptiveAssessmentResponse.builder()
                .conceptName("Dynamic Programming")
                .bktProbabilityAfter(0.85)
                .build();
        when(responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId)).thenReturn(List.of(r1));

        LearnerMasteryDto.Summary masterySummary = LearnerMasteryDto.Summary.builder()
                .masteredSkills(List.of("Arrays", "Dynamic Programming"))
                .developingSkills(List.of("Binary Search"))
                .weakSkills(List.of())
                .revisionRequiredSkills(List.of())
                .build();
        when(masteryService.getMasterySummary(userId)).thenReturn(masterySummary);

        LearnerBehaviorProfile profile = LearnerBehaviorProfile.builder()
                .behaviorCategory("FAST_ACCURATE")
                .behaviorInsights(List.of("High problem-solving speed with strong accuracy."))
                .build();
        when(behaviorService.getBehaviorProfile(userId)).thenReturn(profile);

        AdaptiveAssessmentDto.SessionResultResponse result = assessmentService.getSessionResult(sessionId, user);

        assertNotNull(result);
        assertEquals(83.3, result.getOverallScore(), 0.1);
        assertEquals(ConfidenceLevel.HIGH, result.getConfidenceLevel());
        assertEquals(CourseDifficulty.ADVANCED, result.getDifficultyReached());
        assertEquals("FAST_ACCURATE", result.getBehaviorCategory());
        assertTrue(result.getStrongSkills().contains("Dynamic Programming"));
    }
}
