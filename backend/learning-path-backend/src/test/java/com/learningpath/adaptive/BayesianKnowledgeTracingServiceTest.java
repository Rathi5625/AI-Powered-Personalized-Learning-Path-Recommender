package com.learningpath.adaptive;

import com.learningpath.adaptive.service.BayesianKnowledgeTracingService;
import com.learningpath.config.BktConfig;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.MasteryLevel;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BayesianKnowledgeTracingServiceTest {

    @Spy
    private BktConfig bktConfig = new BktConfig();

    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;

    @InjectMocks
    private BayesianKnowledgeTracingService bktService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("Test Learner")
                .email("test@example.com")
                .build();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    void testComputeNextProbability_CorrectAnswer_IncreasesProbability() {
        double priorPL = 0.20;
        double nextPL = bktService.computeNextProbability(priorPL, true);

        assertTrue(nextPL > priorPL, "Correct answer should increase knowledge probability");
        assertTrue(nextPL <= 0.99, "Probability should remain clamped <= 0.99");
    }

    @Test
    void testComputeNextProbability_IncorrectAnswer_DecreasesProbability() {
        double priorPL = 0.60;
        double nextPL = bktService.computeNextProbability(priorPL, false);

        assertTrue(nextPL < priorPL, "Incorrect answer should decrease knowledge probability");
        assertTrue(nextPL >= 0.01, "Probability should remain clamped >= 0.01");
    }

    @Test
    void testRepeatedCorrectAnswers_TransitionsToMastered() {
        double currentPL = 0.20;
        for (int i = 0; i < 6; i++) {
            currentPL = bktService.computeNextProbability(currentPL, true);
        }

        assertTrue(currentPL >= 0.85, "Repeated correct answers should raise probability to mastery threshold");
        assertEquals(MasteryLevel.MASTERED, bktService.determineMasteryLevel(currentPL));
    }

    @Test
    void testMasteryLevelCategorization() {
        assertEquals(MasteryLevel.NOT_STARTED, bktService.determineMasteryLevel(0.20));
        assertEquals(MasteryLevel.DEVELOPING, bktService.determineMasteryLevel(0.40));
        assertEquals(MasteryLevel.BASIC, bktService.determineMasteryLevel(0.60));
        assertEquals(MasteryLevel.PROFICIENT, bktService.determineMasteryLevel(0.75));
        assertEquals(MasteryLevel.MASTERED, bktService.determineMasteryLevel(0.90));
    }

    @Test
    void testUpdateKnowledgeState_PersistsCorrectly() {
        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(any(), any()))
                .thenReturn(Optional.empty());
        when(knowledgeStateRepository.save(any(LearnerKnowledgeState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LearnerKnowledgeState state = bktService.updateKnowledgeState(testUser, null, "Binary Search", true, 25);

        assertNotNull(state);
        assertEquals("Binary Search", state.getConceptName());
        assertEquals(1, state.getAttempts());
        assertEquals(1, state.getCorrectAttempts());
        assertEquals(0, state.getIncorrectAttempts());
        assertEquals(1, state.getConsecutiveCorrect());
        assertTrue(state.getKnowledgeProbability() > 0.20);
        assertFalse(state.isRevisionRequired());
    }
}
