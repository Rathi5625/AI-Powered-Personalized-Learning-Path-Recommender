package com.learningpath.adaptive;

import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.LearnerBehaviorService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.MasteryLevel;
import com.learningpath.repository.AssessmentResultRepository;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserRepository;
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
public class LearnerMasteryAndBehaviorTest {

    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private AssessmentResultRepository assessmentResultRepository;

    @Mock
    private com.learningpath.repository.AdaptiveAssessmentSessionRepository sessionRepository;

    @Mock
    private com.learningpath.repository.AdaptiveAssessmentResponseRepository responseRepository;

    @InjectMocks
    private LearnerMasteryService masteryService;


    @InjectMocks
    private LearnerBehaviorService behaviorService;

    @Test
    void testMasterySummary_CategorizesMasteredAndWeak() {
        UUID userId = UUID.randomUUID();
        LearnerKnowledgeState k1 = LearnerKnowledgeState.builder()
                .conceptName("Java Basics")
                .knowledgeProbability(0.90)
                .masteryLevel(MasteryLevel.MASTERED)
                .revisionRequired(false)
                .build();
        k1.setId(UUID.randomUUID());

        LearnerKnowledgeState k2 = LearnerKnowledgeState.builder()
                .conceptName("Dynamic Programming")
                .knowledgeProbability(0.25)
                .masteryLevel(MasteryLevel.NOT_STARTED)
                .revisionRequired(true)
                .build();
        k2.setId(UUID.randomUUID());

        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(k1, k2));

        LearnerMasteryDto.Summary summary = masteryService.getMasterySummary(userId);

        assertEquals(2, summary.getTotalConceptsTracked());
        assertTrue(summary.getMasteredSkills().contains("Java Basics"));
        assertTrue(summary.getWeakSkills().contains("Dynamic Programming"));
        assertTrue(summary.getRevisionRequiredSkills().contains("Dynamic Programming"));
    }

    @Test
    void testBehaviorProfile_InsufficientData_HandledExplicitly() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().fullName("New Learner").build();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProgressRepository.findByUserId(userId)).thenReturn(List.of());
        when(assessmentResultRepository.findAllByUserIdOrderByCompletedAtDesc(userId)).thenReturn(List.of());
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of());

        LearnerBehaviorProfile profile = behaviorService.getBehaviorProfile(userId);

        assertNotNull(profile);
        assertTrue(profile.isInsufficientData());
        assertEquals("INSUFFICIENT_DATA", profile.getDataQualityStatus());
    }
}
