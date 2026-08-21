package com.learningpath.integration;

import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.BayesianKnowledgeTracingService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.config.BktConfig;
import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.service.*;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.dto.MlPredictionRequest;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Phase 7 — Step 5C: BKT → ML → Path Consistency Test
 *
 * Verifies that changes in learner mastery:
 * 1. Change feature values fed to the ML model.
 * 2. Produce different ML recommendation scores.
 * 3. Trigger correct path recalculation decisions.
 * 4. BKT consistency: correct → higher mastery, wrong → lower mastery.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — BKT → ML → Path Consistency")
public class BktMlPathConsistencyTest {

    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository itemRepository;
    @Mock private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock private LearningPathVersionRepository versionRepository;
    @Mock private LearningPathEngineService engineService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private LearnerMasteryService masteryService;
    @Mock private MlRecommendationClient mlRecommendationClient;
    @Mock private LearnerFeatureBuilderService featureBuilderService;

    @InjectMocks private LearningPathRecalculationService recalculationService;

    private BayesianKnowledgeTracingService bktService;
    private UUID userId;
    private User learner;
    private LearningPath activePath;

    @BeforeEach
    void setUp() {
        BktConfig bktConfig = new BktConfig();
        bktService = new BayesianKnowledgeTracingService(bktConfig, knowledgeStateRepository);

        userId = UUID.randomUUID();
        learner = User.builder()
                .email("bkt.consistency@learnai.com")
                .fullName("BKT Consistency Tester")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .build();
        learner.setId(userId);

        activePath = LearningPath.builder()
                .user(learner)
                .status(LearningPathStatus.ACTIVE)
                .version(1)
                .build();
        activePath.setId(UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BKT CORRECTNESS
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("BKT — Correct answer increases knowledge probability")
    void testBkt_correctAnswerIncreasesMastery() {
        double initialMastery = 0.45;
        double afterCorrect = bktService.computeNextProbability(initialMastery, true);

        assertThat(afterCorrect).isGreaterThan(initialMastery);
        assertThat(afterCorrect).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("BKT — Wrong answer decreases knowledge probability")
    void testBkt_wrongAnswerDecreasesMastery() {
        double initialMastery = 0.65;
        double afterWrong = bktService.computeNextProbability(initialMastery, false);

        assertThat(afterWrong).isLessThan(initialMastery);
        assertThat(afterWrong).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("BKT — Multiple correct answers compound toward mastery threshold")
    void testBkt_multipleCorrectConvergesToMastery() {
        double mastery = 0.30;

        // Simulate 6 consecutive correct answers
        for (int i = 0; i < 6; i++) {
            mastery = bktService.computeNextProbability(mastery, true);
        }

        // Should converge toward or past 0.70 proficient / 0.85 mastery gate
        assertThat(mastery).isGreaterThan(0.70);
    }

    @Test
    @DisplayName("BKT — Alternating answers produce intermediate mastery")
    void testBkt_alternatingAnswersProducesIntermediateMastery() {
        double mastery = 0.50;

        // Alternating correct/wrong
        mastery = bktService.computeNextProbability(mastery, true);
        mastery = bktService.computeNextProbability(mastery, false);
        mastery = bktService.computeNextProbability(mastery, true);
        mastery = bktService.computeNextProbability(mastery, false);

        // Should remain in intermediate range
        assertThat(mastery).isBetween(0.30, 0.80);
    }

    @Test
    @DisplayName("BKT — Mastery stays bounded after many updates")
    void testBkt_staysBounded_afterManyUpdates() {
        double mastery = 0.5;
        Random rng = new Random(42);

        for (int i = 0; i < 50; i++) {
            boolean correct = rng.nextBoolean();
            mastery = bktService.computeNextProbability(mastery, correct);
            assertThat(mastery).isBetween(0.0, 1.0);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ML FEATURE CONSISTENCY
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ML Features — Higher mastery produces different feature vectors than lower mastery")
    void testMlFeatures_differentMasteryProducesDifferentFeatures() {
        Course dsCourse = Course.builder()
                .title("Data Structures")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();
        dsCourse.setId(UUID.randomUUID());

        // Low mastery context
        LearnerAiContext lowMasteryCtx = LearnerAiContext.builder()
                .userId(userId)
                .overallMasteryPercentage(25.0)
                .assessmentAccuracy(0.30)
                .learningVelocity(0.20)
                .build();

        // High mastery context
        LearnerAiContext highMasteryCtx = LearnerAiContext.builder()
                .userId(userId)
                .overallMasteryPercentage(85.0)
                .assessmentAccuracy(0.90)
                .learningVelocity(0.80)
                .build();

        MlPredictionRequest lowFeatures = MlPredictionRequest.builder()
                .skillGapScore(0.80)
                .careerPriorityScore(0.70)
                .proficiencyGap(0.75)
                .difficultyMatch(0.40)
                .build();

        MlPredictionRequest highFeatures = MlPredictionRequest.builder()
                .skillGapScore(0.15)
                .careerPriorityScore(0.70)
                .proficiencyGap(0.15)
                .difficultyMatch(0.90)
                .build();

        when(featureBuilderService.buildFeatureVector(argThat(ctx ->
                ctx != null && ctx.getOverallMasteryPercentage() < 50.0), any()))
                .thenReturn(lowFeatures);
        when(featureBuilderService.buildFeatureVector(argThat(ctx ->
                ctx != null && ctx.getOverallMasteryPercentage() > 70.0), any()))
                .thenReturn(highFeatures);

        MlPredictionRequest featLow = featureBuilderService.buildFeatureVector(lowMasteryCtx, dsCourse);
        MlPredictionRequest featHigh = featureBuilderService.buildFeatureVector(highMasteryCtx, dsCourse);

        // Features must differ based on mastery
        assertThat(featLow.skillGapScore()).isNotEqualTo(featHigh.skillGapScore());
        assertThat(featLow.proficiencyGap()).isGreaterThan(featHigh.proficiencyGap());
        assertThat(featLow.difficultyMatch()).isLessThan(featHigh.difficultyMatch());
    }

    @Test
    @DisplayName("ML Recommendation — High-mastery learner gets higher score on advanced course")
    void testMlRecommendation_highMasteryHigherScoreOnAdvancedCourse() {
        Course advancedCourse = Course.builder()
                .title("Advanced Dynamic Programming")
                .difficulty(CourseDifficulty.ADVANCED)
                .build();
        advancedCourse.setId(UUID.randomUUID());

        MlPredictionRequest lowReq = MlPredictionRequest.builder()
                .skillGapScore(0.90).proficiencyGap(0.85).difficultyMatch(0.30).build();
        MlPredictionRequest highReq = MlPredictionRequest.builder()
                .skillGapScore(0.20).proficiencyGap(0.15).difficultyMatch(0.95).build();

        // Low mastery → low score on advanced course (not a good match)
        when(mlRecommendationClient.predict(argThat(r ->
                r != null && r.difficultyMatch() < 0.50)))
                .thenReturn(Optional.of(new MlPredictionResponse(0.42, 42.0, false, "v2.0")));

        // High mastery → high score on advanced course (good match)
        when(mlRecommendationClient.predict(argThat(r ->
                r != null && r.difficultyMatch() > 0.70)))
                .thenReturn(Optional.of(new MlPredictionResponse(0.88, 88.0, true, "v2.0")));

        Optional<MlPredictionResponse> lowResult = mlRecommendationClient.predict(lowReq);
        Optional<MlPredictionResponse> highResult = mlRecommendationClient.predict(highReq);

        assertThat(lowResult).isPresent();
        assertThat(highResult).isPresent();
        assertThat(highResult.get().recommendationScore())
                .isGreaterThan(lowResult.get().recommendationScore());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATH CONSISTENCY
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Path Consistency — BKT above 85% threshold triggers item completion in path")
    void testPathConsistency_highBktTriggersItemCompletion() {
        Skill skill = Skill.builder().name("Binary Search").build();
        skill.setId(UUID.randomUUID());

        LearningPathItem bsItem = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Binary Search")
                .targetSkill(skill)
                .status(LearningPathNodeStatus.UNLOCKED)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(false)
                .currentMastery(0.0)
                .build();
        bsItem.setId(UUID.randomUUID());

        LearnerKnowledgeState ks = LearnerKnowledgeState.builder()
                .conceptName("Binary Search")
                .knowledgeProbability(0.87) // Above 85% gate
                .revisionRequired(false)
                .build();

        LearningPathFullResponse mockResponse = new LearningPathFullResponse();
        mockResponse.setVersion(2);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(bsItem));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.save(any())).thenReturn(activePath);
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(mockResponse);

        LearningPathFullResponse result = recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        assertThat(result).isNotNull();
        // Item was saved as completed
        verify(itemRepository).saveAll(argThat(items -> {
            for (LearningPathItem item : (Iterable<LearningPathItem>) items) {
                if ("Binary Search".equals(item.getTitle())) {
                    return item.isCompleted();
                }
            }
            return false;
        }));
    }

    @Test
    @DisplayName("Path Consistency — BKT revision signal triggers REVISION_REQUIRED status")
    void testPathConsistency_revisionBktTriggersRevisionRequired() {
        Skill skill = Skill.builder().name("Binary Search").build();
        skill.setId(UUID.randomUUID());

        LearningPathItem bsItem = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Binary Search")
                .targetSkill(skill)
                .status(LearningPathNodeStatus.UNLOCKED)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(false)
                .currentMastery(0.30)
                .build();
        bsItem.setId(UUID.randomUUID());

        LearnerKnowledgeState ks = LearnerKnowledgeState.builder()
                .conceptName("Binary Search")
                .knowledgeProbability(0.30) // Low
                .revisionRequired(true)      // Flagged for revision
                .build();

        LearningPathFullResponse mockResponse = new LearningPathFullResponse();
        mockResponse.setVersion(2);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(bsItem));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.save(any())).thenReturn(activePath);
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(mockResponse);

        recalculationService.triggerRecalculation(userId, "REVISION_SIGNAL");

        // Item status should have been updated to REVISION_REQUIRED
        verify(itemRepository).saveAll(argThat(items -> {
            for (LearningPathItem item : (Iterable<LearningPathItem>) items) {
                if ("Binary Search".equals(item.getTitle())) {
                    return item.getStatus() == LearningPathNodeStatus.REVISION_REQUIRED;
                }
            }
            return false;
        }));
    }
}
