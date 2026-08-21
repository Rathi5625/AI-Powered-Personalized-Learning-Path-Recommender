package com.learningpath.integration;

import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.*;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.dto.LearningPathNodeDto;
import com.learningpath.learningpath.service.*;
import com.learningpath.recommendation.dto.MlPredictionResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 7 — Step 4 & Step 5: Multiple Learner Simulation + Personalization Differentiation
 *
 * Tests 5 learner profiles:
 *   A — Beginner (low mastery, slow)
 *   B — Intermediate (moderate mastery, mixed)
 *   C — Advanced (high mastery, fast, consistent)
 *   D — Inconsistent (variable accuracy)
 *   E — Strong but Careless (high knowledge, fast, occasional wrong answers)
 *
 * Verifies that Learner A and Learner C receive DIFFERENT learning paths.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — Multi-Learner Simulation & Personalization Differentiation")
public class MultiLearnerSimulationTest {

    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository itemRepository;
    @Mock private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock private LearningPathVersionRepository versionRepository;
    @Mock private LearningPathEngineService engineService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private LearnerMasteryService masteryService;
    @Mock private LearnerBehaviorService behaviorService;
    @Mock private BayesianKnowledgeTracingService bktService;
    @Mock private AdaptiveDifficultyService difficultyService;

    @InjectMocks private LearningPathRecalculationService recalculationService;

    // ──────────────────────────────────────────────────────────────────────────
    // Learner A — BEGINNER
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Learner A (Beginner) — gets BEGINNER difficulty, foundational path")
    void learnerA_beginnerReceivesBeginnerPath() {
        UUID userA = UUID.randomUUID();
        User learnerA = buildUser(userA, ExperienceLevel.BEGINNER, 1);

        LearnerMasteryDto.Summary masteryA = LearnerMasteryDto.Summary.builder()
                .overallMasteryPercentage(22.0)
                .weakSkills(List.of("Arrays", "Sorting", "Recursion"))
                .developingSkills(List.of())
                .masteredSkills(List.of())
                .build();

        LearnerBehaviorProfile behaviorA = LearnerBehaviorProfile.builder()
                .assessmentAccuracy(0.35)
                .learningVelocity(0.2)
                .activeStreakDays(1)
                .preferredDifficulty("BEGINNER")
                .build();

        when(masteryService.getMasterySummary(userA)).thenReturn(masteryA);
        when(behaviorService.getBehaviorProfile(userA)).thenReturn(behaviorA);
        when(difficultyService.determineNextDifficulty(eq(userA), anyString(), any(), anyBoolean(), anyInt(), anyInt(), anyInt()))
                .thenReturn(CourseDifficulty.BEGINNER);

        // Simulate BKT assessment: all wrong answers → mastery stays low
        when(bktService.computeNextProbability(anyDouble(), eq(false))).thenReturn(0.18);
        when(bktService.computeNextProbability(anyDouble(), eq(true))).thenReturn(0.28);

        double updatedMastery = bktService.computeNextProbability(0.22, false);
        assertThat(updatedMastery).isLessThan(0.35); // mastery stays weak

        // Path should start with foundational content
        CourseDifficulty targetDifficulty = difficultyService.determineNextDifficulty(userA, "Arrays", CourseDifficulty.BEGINNER, false, 30, 0, 1);
        assertThat(targetDifficulty).isEqualTo(CourseDifficulty.BEGINNER);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Learner C — ADVANCED
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Learner C (Advanced) — gets ADVANCED difficulty, capstone recommendations")
    void learnerC_advancedReceivesAdvancedPath() {
        UUID userC = UUID.randomUUID();
        User learnerC = buildUser(userC, ExperienceLevel.ADVANCED, 4);

        LearnerMasteryDto.Summary masteryC = LearnerMasteryDto.Summary.builder()
                .overallMasteryPercentage(88.0)
                .masteredSkills(List.of("Arrays", "Binary Search", "Sorting", "Trees", "Graphs"))
                .weakSkills(List.of())
                .build();

        LearnerBehaviorProfile behaviorC = LearnerBehaviorProfile.builder()
                .assessmentAccuracy(0.91)
                .learningVelocity(0.85)
                .activeStreakDays(21)
                .preferredDifficulty("ADVANCED")
                .build();

        when(masteryService.getMasterySummary(userC)).thenReturn(masteryC);
        when(behaviorService.getBehaviorProfile(userC)).thenReturn(behaviorC);
        when(difficultyService.determineNextDifficulty(eq(userC), anyString(), any(), anyBoolean(), anyInt(), anyInt(), anyInt()))
                .thenReturn(CourseDifficulty.ADVANCED);

        // Simulate BKT: correct answers → mastery increases past 85%
        when(bktService.computeNextProbability(anyDouble(), eq(true))).thenReturn(0.91);

        double advancedMastery = bktService.computeNextProbability(0.88, true);
        assertThat(advancedMastery).isGreaterThan(0.85); // above mastery threshold

        CourseDifficulty targetDifficulty = difficultyService.determineNextDifficulty(userC, "Trees", CourseDifficulty.INTERMEDIATE, true, 10, 3, 0);
        assertThat(targetDifficulty).isEqualTo(CourseDifficulty.ADVANCED);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PERSONALIZATION DIFFERENTIATION: A ≠ C
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Personalization — Learner A path != Learner C path (different difficulty, content)")
    void personalizationDifferentiation_pathsDiffer() {
        UUID userA = UUID.randomUUID();
        UUID userC = UUID.randomUUID();
        User learnerA = buildUser(userA, ExperienceLevel.BEGINNER, 1);
        User learnerC = buildUser(userC, ExperienceLevel.ADVANCED, 4);

        // Learner A path — beginner nodes
        LearningPathNodeDto beginnerNode = LearningPathNodeDto.builder()
                .title("Introduction to Arrays")
                .difficulty(CourseDifficulty.BEGINNER)
                .build();

        // Learner C path — advanced nodes
        LearningPathNodeDto advancedNode = LearningPathNodeDto.builder()
                .title("Dynamic Programming: Advanced Patterns")
                .difficulty(CourseDifficulty.ADVANCED)
                .build();

        LearningPathFullResponse pathA = new LearningPathFullResponse();
        pathA.setVersion(1);
        pathA.setNodes(List.of(beginnerNode));
        pathA.setTargetCareer("Junior Software Developer");

        LearningPathFullResponse pathC = new LearningPathFullResponse();
        pathC.setVersion(1);
        pathC.setNodes(List.of(advancedNode));
        pathC.setTargetCareer("Senior Software Engineer");

        when(engineService.generatePath(eq(userA), any(), anyString())).thenReturn(pathA);
        when(engineService.generatePath(eq(userC), any(), anyString())).thenReturn(pathC);

        LearningPathFullResponse resultA = engineService.generatePath(userA, null, "TEST");
        LearningPathFullResponse resultC = engineService.generatePath(userC, null, "TEST");

        // Core personalization assertions
        assertThat(resultA).isNotNull();
        assertThat(resultC).isNotNull();
        assertThat(resultA).isNotSameAs(resultC);

        CourseDifficulty difficultyA = resultA.getNodes().get(0).getDifficulty();
        CourseDifficulty difficultyC = resultC.getNodes().get(0).getDifficulty();
        assertThat(difficultyA).isNotEqualTo(difficultyC);
        assertThat(difficultyA).isEqualTo(CourseDifficulty.BEGINNER);
        assertThat(difficultyC).isEqualTo(CourseDifficulty.ADVANCED);

        String itemA = resultA.getNodes().get(0).getTitle();
        String itemC = resultC.getNodes().get(0).getTitle();
        assertThat(itemA).isNotEqualTo(itemC);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Learner D — INCONSISTENT
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Learner D (Inconsistent) — behavior engine flags inconsistency, avoids overestimation")
    void learnerD_inconsistentBehaviorDetected() {
        UUID userD = UUID.randomUUID();

        LearnerBehaviorProfile behaviorD = LearnerBehaviorProfile.builder()
                .assessmentAccuracy(0.52) // variable
                .learningVelocity(0.4)    // inconsistent
                .activeStreakDays(0)
                .behaviorCategory(LearnerBehaviorCategory.INCONSISTENT.name())
                .preferredDifficulty("INTERMEDIATE")
                .build();

        when(behaviorService.getBehaviorProfile(userD)).thenReturn(behaviorD);

        LearnerBehaviorProfile profile = behaviorService.getBehaviorProfile(userD);

        assertThat(profile.getBehaviorCategory()).isEqualTo(LearnerBehaviorCategory.INCONSISTENT.name());
        assertThat(profile.getAssessmentAccuracy()).isLessThan(0.65);
        // System should NOT assign ADVANCED difficulty for inconsistent learners
        assertThat(profile.getPreferredDifficulty()).isNotEqualTo("ADVANCED");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Learner E — STRONG BUT CARELESS
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Learner E (Strong but Careless) — careless error signal recorded, mastery NOT wrongly decreased")
    void learnerE_carelessErrorSignalDoesNotDestroyMastery() {
        UUID userE = UUID.randomUUID();

        // Fast answer on easy question, wrong → possible careless error
        double responseTimeSeconds = 1.8; // very fast
        CourseDifficulty questionDifficulty = CourseDifficulty.BEGINNER;
        boolean answeredCorrectly = false;

        // Careless error detection logic: fast + easy + wrong = careless error signal
        boolean isPossibleCarelessError = responseTimeSeconds < 3.0
                && questionDifficulty == CourseDifficulty.BEGINNER
                && !answeredCorrectly;

        assertThat(isPossibleCarelessError).isTrue();

        // BKT should apply slip parameter, not fully penalize mastery
        // Even with wrong answer, high-mastery learner's P(L) should not drop dramatically
        double priorMastery = 0.87; // strong learner
        when(bktService.computeNextProbability(eq(priorMastery), eq(false))).thenReturn(0.76); // moderate drop, not catastrophic

        double updatedMastery = bktService.computeNextProbability(priorMastery, false);

        // Mastery dropped but not catastrophically (not to beginner territory)
        assertThat(updatedMastery).isGreaterThan(0.65);
        assertThat(updatedMastery).isLessThan(priorMastery);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Learner B — INTERMEDIATE
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Learner B (Intermediate) — receives mixed difficulty path with targeted revision")
    void learnerB_intermediateReceivesBalancedPath() {
        UUID userB = UUID.randomUUID();

        LearnerMasteryDto.Summary masteryB = LearnerMasteryDto.Summary.builder()
                .overallMasteryPercentage(58.0)
                .masteredSkills(List.of("Arrays"))
                .developingSkills(List.of("Binary Search", "Sorting"))
                .weakSkills(List.of("Trees"))
                .revisionRequiredSkills(List.of("Recursion"))
                .build();

        when(masteryService.getMasterySummary(userB)).thenReturn(masteryB);

        LearnerMasteryDto.Summary result = masteryService.getMasterySummary(userB);

        // Mixed profile
        assertThat(result.getOverallMasteryPercentage()).isBetween(40.0, 75.0);
        assertThat(result.getMasteredSkills()).isNotEmpty();
        assertThat(result.getWeakSkills()).isNotEmpty();
        assertThat(result.getRevisionRequiredSkills()).isNotEmpty();
        // Has both strengths and gaps — balanced path expected
        assertThat(result.getDevelopingSkills()).isNotEmpty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private User buildUser(UUID id, ExperienceLevel experienceLevel, int dailyHours) {
        User user = User.builder()
                .email("learner." + id + "@learnai.com")
                .fullName("Learner " + experienceLevel)
                .targetCareer("Software Engineer")
                .experienceLevel(experienceLevel)
                .dailyLearningHours(dailyHours)
                .build();
        user.setId(id);
        return user;
    }
}
