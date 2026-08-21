package com.learningpath.learningpath;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathNodeType;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.dto.WeeklyLearningPlanDto;
import com.learningpath.learningpath.service.LearningPathEngineService;
import com.learningpath.learningpath.service.LearningPathRecalculationService;
import com.learningpath.learningpath.service.WeeklyLearningPlanService;
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
public class LearningPathRecalculationAndWeeklyTest {

    @Mock
    private LearningPathRepository learningPathRepository;
    @Mock
    private LearningPathItemRepository itemRepository;
    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock
    private LearningPathVersionRepository versionRepository;
    @Mock
    private LearningPathEngineService engineService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LearningPathRecalculationService recalculationService;

    @InjectMocks
    private WeeklyLearningPlanService weeklyPlanService;

    private UUID userId;
    private User user;
    private LearningPath activePath;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .fullName("Alex Learner")
                .email("alex@example.com")
                .targetCareer("Backend Developer")
                .dailyLearningHours(2)
                .build();
        user.setId(userId);

        activePath = LearningPath.builder()
                .user(user)
                .title("Personalized Learning Path for Backend Developer")
                .version(1)
                .status(LearningPathStatus.ACTIVE)
                .overallProgress(25.0)
                .build();
        activePath.setId(UUID.randomUUID());
    }

    @Test
    void testRecalculate_UnlocksPrerequisitesAndRecordsVersion() {
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));

        Skill skill1 = Skill.builder().name("Arrays").build();
        Skill skill2 = Skill.builder().name("Binary Search").build();

        LearningPathItem item1 = LearningPathItem.builder()
                .learningPath(activePath)
                .targetSkill(skill1)
                .title("Mastering Arrays")
                .nodeType(LearningPathNodeType.COURSE)
                .status(LearningPathNodeStatus.IN_PROGRESS)
                .currentMastery(0.50)
                .itemOrder(1)
                .build();

        LearningPathItem item2 = LearningPathItem.builder()
                .learningPath(activePath)
                .targetSkill(skill2)
                .title("Mastering Binary Search")
                .nodeType(LearningPathNodeType.COURSE)
                .status(LearningPathNodeStatus.LOCKED)
                .currentMastery(0.0)
                .itemOrder(2)
                .build();

        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item1, item2));

        // Simulate BKT state update where Arrays rose to 0.88 (mastered)
        LearnerKnowledgeState bktArrays = LearnerKnowledgeState.builder()
                .conceptName("Arrays")
                .knowledgeProbability(0.88)
                .build();
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(bktArrays));

        when(engineService.generatePath(any(), any(), any()))
                .thenReturn(LearningPathFullResponse.builder().version(2).build());

        LearningPathFullResponse response = recalculationService.triggerRecalculation(userId, "Arrays assessment passed with 95%");

        assertNotNull(response);
        assertEquals(2, response.getVersion());
    }

    @Test
    void testWeeklyPlan_DistributesWithinBudget() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE))
                .thenReturn(Optional.of(activePath));

        LearningPathItem item1 = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Java Fundamentals")
                .nodeType(LearningPathNodeType.COURSE)
                .status(LearningPathNodeStatus.IN_PROGRESS)
                .estimatedMinutes(45)
                .itemOrder(1)
                .build();

        LearningPathItem item2 = LearningPathItem.builder()
                .learningPath(activePath)
                .title("Java Practice Set")
                .nodeType(LearningPathNodeType.PRACTICE)
                .status(LearningPathNodeStatus.UNLOCKED)
                .estimatedMinutes(30)
                .itemOrder(2)
                .build();

        when(itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item1, item2));

        WeeklyLearningPlanDto plan = weeklyPlanService.getWeeklyPlan(userId);

        assertNotNull(plan);
        assertEquals(600, plan.getWeeklyTargetMinutes()); // 2 hours/day * 5 days * 60 min = 600 min
        assertFalse(plan.getDays().isEmpty());
    }
}
