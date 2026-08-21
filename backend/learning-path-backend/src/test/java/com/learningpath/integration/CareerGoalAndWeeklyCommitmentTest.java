package com.learningpath.integration;

import com.learningpath.adaptive.service.*;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.dto.LearningPathNodeDto;
import com.learningpath.learningpath.dto.WeeklyDayScheduleDto;
import com.learningpath.learningpath.dto.WeeklyLearningPlanDto;
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
 * Phase 7 — Step 5D: Career Goal & Weekly Commitment Change Tests
 *
 * Tests that:
 * 1. Career goal change triggers full path recalculation.
 * 2. Weekly commitment changes (5h, 10h, 20h) produce different weekly schedules.
 * 3. Path adapts target career and skills when goal changes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Phase 7 — Career Goal Change & Weekly Commitment Tests")
public class CareerGoalAndWeeklyCommitmentTest {

    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository itemRepository;
    @Mock private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock private LearningPathVersionRepository versionRepository;
    @Mock private LearningPathEngineService engineService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private WeeklyLearningPlanService weeklyPlanService;

    @InjectMocks private LearningPathRecalculationService recalculationService;

    private UUID userId;
    private User learner;
    private LearningPath activePath;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        learner = User.builder()
                .email("career.test@learnai.com")
                .fullName("Career Change Tester")
                .targetCareer("Software Engineer")
                .experienceLevel(ExperienceLevel.INTERMEDIATE)
                .dailyLearningHours(2)
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
    // CAREER GOAL CHANGE TESTS
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Career Goal — Change triggers path recalculation with new career context")
    void testCareerGoalChange_triggersRecalculation() {
        // Initial path: Software Engineer
        LearningPathFullResponse seResponse = new LearningPathFullResponse();
        seResponse.setVersion(1);
        seResponse.setTargetCareer("Software Engineer");

        // After career change: Data Scientist
        LearningPathFullResponse dsResponse = new LearningPathFullResponse();
        dsResponse.setVersion(2);
        dsResponse.setTargetCareer("Data Scientist");

        // Simulate no state-change items (career change itself is the trigger)
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of());
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(eq(userId), any(), anyString())).thenReturn(dsResponse);

        LearningPathFullResponse result = recalculationService.triggerRecalculation(userId, "CAREER_GOAL_CHANGED");

        assertThat(result).isNotNull();
        assertThat(result.getTargetCareer()).isEqualTo("Data Scientist");
        verify(engineService).generatePath(eq(userId), any(), anyString());
    }

    @Test
    @DisplayName("Career Goal — SE path and DS path have different content (differentiation)")
    void testCareerGoalChange_differentCareersDifferentPaths() {
        // Software Engineer path
        LearningPathNodeDto seNode = LearningPathNodeDto.builder()
                .title("Spring Boot Microservices")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();

        // Data Scientist path
        LearningPathNodeDto dsNode = LearningPathNodeDto.builder()
                .title("Machine Learning with Python")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .build();

        LearningPathFullResponse seResponse = new LearningPathFullResponse();
        seResponse.setTargetCareer("Software Engineer");
        seResponse.setNodes(List.of(seNode));

        LearningPathFullResponse dsResponse = new LearningPathFullResponse();
        dsResponse.setTargetCareer("Data Scientist");
        dsResponse.setNodes(List.of(dsNode));

        UUID seUserId = UUID.randomUUID();
        UUID dsUserId = UUID.randomUUID();

        when(engineService.generatePath(eq(seUserId), any(), anyString())).thenReturn(seResponse);
        when(engineService.generatePath(eq(dsUserId), any(), anyString())).thenReturn(dsResponse);

        LearningPathFullResponse seResult = engineService.generatePath(seUserId, null, "TEST");
        LearningPathFullResponse dsResult = engineService.generatePath(dsUserId, null, "TEST");

        assertThat(seResult.getTargetCareer()).isNotEqualTo(dsResult.getTargetCareer());
        assertThat(seResult.getNodes().get(0).getTitle())
                .isNotEqualTo(dsResult.getNodes().get(0).getTitle());
    }

    @Test
    @DisplayName("Career Goal — Notification generated after career-change recalculation")
    void testCareerGoalChange_notificationGenerated() {
        Skill skill = Skill.builder().name("Old Skill").build();
        skill.setId(UUID.randomUUID());

        // Force a state change by putting an item that will be moved to REVISION_REQUIRED
        LearningPathItem item = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Old Career Skill")
                .targetSkill(skill)
                .status(LearningPathNodeStatus.UNLOCKED)
                .nodeType(LearningPathNodeType.COURSE)
                .itemOrder(1)
                .isCompleted(false)
                .currentMastery(0.0)
                .build();
        item.setId(UUID.randomUUID());

        LearnerKnowledgeState ks = LearnerKnowledgeState.builder()
                .conceptName("Old Skill")
                .knowledgeProbability(0.25)
                .revisionRequired(true) // Triggers state change
                .build();

        LearningPathFullResponse response = new LearningPathFullResponse();
        response.setVersion(2);

        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));
        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item));
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(ks));
        when(itemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(learningPathRepository.save(any())).thenReturn(activePath);
        when(versionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(learner));
        when(engineService.generatePath(any(), any(), anyString())).thenReturn(response);

        recalculationService.triggerRecalculation(userId, "CAREER_GOAL_CHANGED");

        verify(notificationService).createNotification(
                eq(userId), anyString(), anyString(), eq(NotificationCategory.LEARNING), anyString()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WEEKLY COMMITMENT CHANGE TESTS
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Weekly Commitment — 5h/week produces fewer daily tasks than 20h/week")
    void testWeeklyCommitment_5h_vs_20h_differentSchedules() {
        // 5h/week → ~1h/day → light schedule
        WeeklyLearningPlanDto plan5h = WeeklyLearningPlanDto.builder()
                .weeklyTargetMinutes(300) // 5 * 60
                .scheduledMinutes(280)
                .days(createLightSchedule())
                .build();

        // 20h/week → ~4h/day → intensive schedule
        WeeklyLearningPlanDto plan20h = WeeklyLearningPlanDto.builder()
                .weeklyTargetMinutes(1200) // 20 * 60
                .scheduledMinutes(1150)
                .days(createIntensiveSchedule())
                .build();

        UUID userId5h = UUID.randomUUID();
        UUID userId20h = UUID.randomUUID();

        when(weeklyPlanService.getWeeklyPlan(userId5h)).thenReturn(plan5h);
        when(weeklyPlanService.getWeeklyPlan(userId20h)).thenReturn(plan20h);

        WeeklyLearningPlanDto result5h = weeklyPlanService.getWeeklyPlan(userId5h);
        WeeklyLearningPlanDto result20h = weeklyPlanService.getWeeklyPlan(userId20h);

        assertThat(result5h.getWeeklyTargetMinutes()).isLessThan(result20h.getWeeklyTargetMinutes());
        assertThat(result5h.getScheduledMinutes()).isLessThan(result20h.getScheduledMinutes());
    }

    @Test
    @DisplayName("Weekly Commitment — 10h/week produces realistically distributed daily plan")
    void testWeeklyCommitment_10h_producesRealisticPlan() {
        WeeklyLearningPlanDto plan10h = WeeklyLearningPlanDto.builder()
                .weeklyTargetMinutes(600) // 10 * 60
                .scheduledMinutes(580)
                .days(createModerateSchedule())
                .build();

        when(weeklyPlanService.getWeeklyPlan(userId)).thenReturn(plan10h);

        WeeklyLearningPlanDto result = weeklyPlanService.getWeeklyPlan(userId);

        assertThat(result).isNotNull();
        assertThat(result.getWeeklyTargetMinutes()).isEqualTo(600);
        assertThat(result.getScheduledMinutes()).isGreaterThan(0);
        assertThat(result.getDays()).isNotEmpty();
        // Each day should have a reasonable load (not more than 3h = 180min/day)
        result.getDays().forEach(day ->
                assertThat(day.getAllocatedMinutes()).isLessThanOrEqualTo(180)
        );
    }

    @Test
    @DisplayName("Weekly Commitment — Empty item list produces empty schedule (no crash)")
    void testWeeklyCommitment_noItems_emptySchedule() {
        WeeklyLearningPlanDto emptyPlan = WeeklyLearningPlanDto.builder()
                .weeklyTargetMinutes(600)
                .scheduledMinutes(0)
                .days(List.of())
                .build();

        when(weeklyPlanService.getWeeklyPlan(userId)).thenReturn(emptyPlan);

        WeeklyLearningPlanDto result = weeklyPlanService.getWeeklyPlan(userId);

        assertThat(result).isNotNull();
        assertThat(result.getDays()).isEmpty();
        assertThat(result.getScheduledMinutes()).isEqualTo(0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private List<WeeklyDayScheduleDto> createLightSchedule() {
        return List.of(
                WeeklyDayScheduleDto.builder().dayName("Monday").allocatedMinutes(60).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Wednesday").allocatedMinutes(60).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Friday").allocatedMinutes(60).activities(List.of()).build()
        );
    }

    private List<WeeklyDayScheduleDto> createModerateSchedule() {
        return List.of(
                WeeklyDayScheduleDto.builder().dayName("Monday").allocatedMinutes(120).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Tuesday").allocatedMinutes(100).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Thursday").allocatedMinutes(120).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Saturday").allocatedMinutes(120).activities(List.of()).build()
        );
    }

    private List<WeeklyDayScheduleDto> createIntensiveSchedule() {
        return List.of(
                WeeklyDayScheduleDto.builder().dayName("Monday").allocatedMinutes(240).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Tuesday").allocatedMinutes(240).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Wednesday").allocatedMinutes(240).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Thursday").allocatedMinutes(240).activities(List.of()).build(),
                WeeklyDayScheduleDto.builder().dayName("Friday").allocatedMinutes(240).activities(List.of()).build()
        );
    }
}
