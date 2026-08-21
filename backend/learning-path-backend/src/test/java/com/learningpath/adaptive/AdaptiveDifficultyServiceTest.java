package com.learningpath.adaptive;

import com.learningpath.adaptive.service.AdaptiveDifficultyService;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.MasteryLevel;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdaptiveDifficultyServiceTest {

    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;

    @InjectMocks
    private AdaptiveDifficultyService difficultyService;

    @Test
    void testDetermineDifficulty_NoState_ReturnsDefault() {
        UUID userId = UUID.randomUUID();
        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(eq(userId), any()))
                .thenReturn(Optional.empty());

        CourseDifficulty diff = difficultyService.determineDifficulty(userId, "Arrays", CourseDifficulty.BEGINNER);
        assertEquals(CourseDifficulty.BEGINNER, diff);
    }

    @Test
    void testDetermineDifficulty_HighStreaksAndMastery_UpgradesToAdvanced() {
        UUID userId = UUID.randomUUID();
        LearnerKnowledgeState state = LearnerKnowledgeState.builder()
                .conceptName("Arrays")
                .knowledgeProbability(0.88)
                .consecutiveCorrect(3)
                .consecutiveIncorrect(0)
                .masteryLevel(MasteryLevel.MASTERED)
                .build();

        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(eq(userId), eq("Arrays")))
                .thenReturn(Optional.of(state));

        CourseDifficulty diff = difficultyService.determineDifficulty(userId, "Arrays", CourseDifficulty.BEGINNER);
        assertEquals(CourseDifficulty.ADVANCED, diff);
    }

    @Test
    void testDetermineDifficulty_ConsecutiveFailures_DowngradesToBeginner() {
        UUID userId = UUID.randomUUID();
        LearnerKnowledgeState state = LearnerKnowledgeState.builder()
                .conceptName("Trees")
                .knowledgeProbability(0.40)
                .consecutiveCorrect(0)
                .consecutiveIncorrect(2)
                .masteryLevel(MasteryLevel.DEVELOPING)
                .build();

        when(knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(eq(userId), eq("Trees")))
                .thenReturn(Optional.of(state));

        CourseDifficulty diff = difficultyService.determineDifficulty(userId, "Trees", CourseDifficulty.INTERMEDIATE);
        assertEquals(CourseDifficulty.BEGINNER, diff);
    }
}
