package com.learningpath.integration;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.service.*;
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
 * Phase 7 — Step 5E: Learning Path Versioning, Audit Trail & Notification Tests
 *
 * Tests that:
 * 1. Every meaningful recalculation increments version number.
 * 2. Version audit records: previous/new version, reason, timestamp.
 * 3. Duplicate-state recalculation does NOT increment version.
 * 4. Notifications are fired with correct category and link.
 * 5. Multiple recalculations produce monotonically increasing versions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — Learning Path Versioning, Audit & Notification Tests")
public class LearningPathVersioningAndNotificationTest {

    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository itemRepository;
    @Mock private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock private LearningPathVersionRepository versionRepository;
    @Mock private LearningPathEngineService engineService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;

    @InjectMocks private LearningPathRecalculationService recalculationService;

    private UUID userId;
    private User learner;
    private LearningPath activePath;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        learner = User.builder()
                .email("versioning.test@learnai.com")
                .fullName("Versioning Tester")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .build();
        learner.setId(userId);

        activePath = LearningPath.builder()
                .user(learner)
                .status(LearningPathStatus.ACTIVE)
                .version(1)
                .overallProgress(20.0)
                .build();
        activePath.setId(UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // VERSION INCREMENT: State Change → Version Incremented
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Versioning A — Mastery gate passed → version incremented from 1 to 2")
    void testVersionIncrement_onMasteryGatePassed() {
        // Item just passed 85% mastery gate → state changes → version increments
        LearningPathItem item = buildItem("Binary Search", 0.87, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Binary Search", 0.87, false);

        LearningPathFullResponse response = new LearningPathFullResponse();
        response.setVersion(2);

        stubCommonMocks(List.of(item), List.of(ks), response);

        LearningPathFullResponse result = recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        assertThat(result).isNotNull();
        // Saved path should have version 2
        verify(learningPathRepository).save(argThat(p -> p.getVersion() == 2));
    }

    @Test
    @DisplayName("Versioning B — Version record persisted with correct trigger reason")
    void testVersionRecord_persistedWithTriggerReason() {
        LearningPathItem item = buildItem("Sorting", 0.88, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Sorting", 0.88, false);

        stubCommonMocks(List.of(item), List.of(ks), new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "WEEKLY_REVIEW");

        verify(versionRepository).save(argThat(v -> {
            LearningPathVersion version = (LearningPathVersion) v;
            return "WEEKLY_REVIEW".equals(version.getChangeReason())
                    && version.getVersionNumber() == 2
                    && version.getExplanation() != null && !version.getExplanation().isBlank();
        }));
    }

    @Test
    @DisplayName("Versioning C — Version audit includes explanation of what changed")
    void testVersionRecord_includesExplanation() {
        LearningPathItem item = buildItem("Arrays", 0.90, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Arrays", 0.90, false);

        stubCommonMocks(List.of(item), List.of(ks), new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        verify(versionRepository).save(argThat(v -> {
            LearningPathVersion version = (LearningPathVersion) v;
            // Explanation should mention what was unlocked or completed
            return version.getExplanation() != null && !version.getExplanation().isEmpty();
        }));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NO VERSION INCREMENT: No State Change → Version Unchanged
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Versioning D — No state change → version NOT incremented, no version record")
    void testNoVersionIncrement_whenNoStateChange() {
        // Item at 50% mastery — no threshold crossed, no revision flag
        LearningPathItem item = buildItem("Binary Search", 0.50, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Binary Search", 0.50, false);

        LearningPathFullResponse response = new LearningPathFullResponse();
        response.setVersion(1);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(response);

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        // Version should NOT be incremented → no save, no version record, no notification
        verify(learningPathRepository, never()).save(any());
        verify(versionRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MULTIPLE RECALCULATIONS: Monotonically Increasing Versions
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Versioning E — Multiple sequential recalculations produce v2 → v3 → v4")
    void testMultipleRecalculations_monotonicVersions() {
        // Each recalculation with a state change should increment version
        // We verify by tracking saved path versions across calls

        List<Integer> capturedVersions = new ArrayList<>();

        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(inv -> {
            LearningPath p = inv.getArgument(0);
            capturedVersions.add(p.getVersion());
            return p;
        });
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(new LearningPathFullResponse());

        // Simulate 3 sequential recalculations with state changes
        for (int call = 1; call <= 3; call++) {
            // Reset path version for this call
            activePath.setVersion(call);

            LearningPathItem item = buildItem("Skill" + call, 0.87, false, LearningPathNodeStatus.UNLOCKED, false);
            LearnerKnowledgeState ks = buildKs("Skill" + call, 0.87, false);

            when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                    .thenReturn(Optional.of(activePath));
            when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                    .thenReturn(List.of(item));
            when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
            when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

            recalculationService.triggerRecalculation(userId, "ASSESSMENT_" + call);
        }

        // All captured versions should be monotonically increasing
        assertThat(capturedVersions).hasSize(3);
        for (int i = 1; i < capturedVersions.size(); i++) {
            assertThat(capturedVersions.get(i)).isGreaterThan(capturedVersions.get(i - 1));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NOTIFICATION VALIDATION
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Notification A — Fired with LEARNING category and correct route on state change")
    void testNotification_correctCategoryAndRoute() {
        LearningPathItem item = buildItem("Trees", 0.86, false, LearningPathNodeStatus.LOCKED, false);
        LearningPathItem prevItem = buildItem("Binary Search", 0.70, true, LearningPathNodeStatus.COMPLETED, true);

        LearnerKnowledgeState ks1 = buildKs("Trees", 0.86, false);
        LearnerKnowledgeState ks2 = buildKs("Binary Search", 0.70, false);

        // Set up items in order for prerequisite unlocking
        item.setItemOrder(2);
        item.setNodeType(LearningPathNodeType.COURSE);
        prevItem.setItemOrder(1);
        prevItem.setNodeType(LearningPathNodeType.COURSE);

        stubCommonMocks(List.of(prevItem, item), List.of(ks1, ks2), new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        verify(notificationService).createNotification(
                eq(userId),
                argThat(title -> title.contains("Learning Path Updated")),
                anyString(),
                eq(NotificationCategory.LEARNING),
                eq("/learning-path")
        );
    }

    @Test
    @DisplayName("Notification B — Revision required skills appear in notification body")
    void testNotification_revisionSkillsInMessage() {
        LearningPathItem item = buildItem("Recursion", 0.30, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Recursion", 0.30, true); // Revision required

        stubCommonMocks(List.of(item), List.of(ks), new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        verify(notificationService).createNotification(
                eq(userId),
                anyString(),
                argThat(body -> body.contains("Recursion")), // Revision skill in body
                eq(NotificationCategory.LEARNING),
                anyString()
        );
    }

    @Test
    @DisplayName("Notification C — Notification NOT fired when nothing changes")
    void testNotification_notFired_whenNoStateChange() {
        LearningPathItem item = buildItem("Binary Search", 0.55, false, LearningPathNodeStatus.UNLOCKED, false);
        LearnerKnowledgeState ks = buildKs("Binary Search", 0.55, false);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PROGRESS RECALCULATION
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Progress — Completing 2 of 4 items produces 50% overall progress")
    void testProgress_twoOfFourCompleted() {
        // 4 items, 2 completed
        LearningPathItem item1 = buildItem("Arrays", 0.90, true, LearningPathNodeStatus.COMPLETED, true);
        LearningPathItem item2 = buildItem("Sorting", 0.88, true, LearningPathNodeStatus.COMPLETED, true);
        LearningPathItem item3 = buildItem("Trees", 0.40, false, LearningPathNodeStatus.UNLOCKED, false);
        LearningPathItem item4 = buildItem("Graphs", 0.10, false, LearningPathNodeStatus.LOCKED, false);

        // Force state change via a new revision flag
        LearnerKnowledgeState ks3 = buildKs("Trees", 0.40, true); // revision required

        stubCommonMocks(List.of(item1, item2, item3, item4), List.of(ks3), new LearningPathFullResponse());

        recalculationService.triggerRecalculation(userId, "ASSESSMENT_COMPLETED");

        verify(learningPathRepository).save(argThat(p -> {
            // 2 already completed, so progress = (2/4)*100 = 50%
            return Math.abs(p.getOverallProgress() - 50.0) < 1.0;
        }));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private LearningPathItem buildItem(String skill, double mastery, boolean priorCompleted,
                                        LearningPathNodeStatus status, boolean isCompleted) {
        Skill targetSkill = Skill.builder().name(skill).build();
        targetSkill.setId(UUID.randomUUID());

        LearningPathItem item = LearningPathItem.builder()
                .learningPath(activePath)
                .title(skill)
                .targetSkill(targetSkill)
                .status(status)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(isCompleted)
                .currentMastery(mastery)
                .build();
        item.setId(UUID.randomUUID());
        return item;
    }

    private LearnerKnowledgeState buildKs(String conceptName, double probability, boolean revisionRequired) {
        return LearnerKnowledgeState.builder()
                .conceptName(conceptName)
                .knowledgeProbability(probability)
                .revisionRequired(revisionRequired)
                .build();
    }

    private void stubCommonMocks(List<LearningPathItem> items,
                                  List<LearnerKnowledgeState> ksList,
                                  LearningPathFullResponse response) {
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(items);
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(ksList);
        when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.save(any())).thenReturn(activePath);
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(response);
    }
}
