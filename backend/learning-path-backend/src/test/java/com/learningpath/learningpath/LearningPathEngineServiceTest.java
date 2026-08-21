package com.learningpath.learningpath;

import com.learningpath.adaptive.service.AdaptiveDifficultyService;
import com.learningpath.adaptive.service.BayesianKnowledgeTracingService;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathNodeType;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.learningpath.dto.SkillGapDetailDto;
import com.learningpath.learningpath.service.CareerSkillGapService;
import com.learningpath.learningpath.service.LearningPathEngineService;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.repository.*;
import com.learningpath.skilldependency.dto.LearningOrderResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
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
public class LearningPathEngineServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseSkillRepository courseSkillRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private LearningPathRepository learningPathRepository;
    @Mock
    private LearningPathItemRepository learningPathItemRepository;
    @Mock
    private LearningPathVersionRepository versionRepository;
    @Mock
    private CareerSkillGapService gapService;
    @Mock
    private SkillDependencyService dependencyService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private AdaptiveDifficultyService difficultyService;
    @Mock
    private BayesianKnowledgeTracingService bktService;
    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;
    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private LearningPathEngineService engineService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .fullName("Jane Learner")
                .email("jane@example.com")
                .targetCareer("Software Engineer")
                .dailyLearningHours(2)
                .build();
        user.setId(userId);
    }

    @Test
    void testGeneratePath_TopologicalOrderAndMasteryGates() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<SkillGapDetailDto> mockGaps = List.of(
                SkillGapDetailDto.builder().skill("Arrays").currentMastery(0.75).requiredLevel(0.80).gap(0.05).priority(0.9).status("PROFICIENT").build(),
                SkillGapDetailDto.builder().skill("Binary Search").currentMastery(0.40).requiredLevel(0.80).gap(0.40).priority(0.8).status("DEVELOPING").build(),
                SkillGapDetailDto.builder().skill("Trees").currentMastery(0.10).requiredLevel(0.70).gap(0.60).priority(0.7).status("NOT_STARTED").build()
        );
        when(gapService.analyzeGaps(userId, null)).thenReturn(mockGaps);

        LearningOrderResponse order = LearningOrderResponse.ok(List.of("Arrays", "Binary Search", "Trees"), List.of());
        when(dependencyService.getLearningOrder(any())).thenReturn(order);


        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of());
        when(userProgressRepository.findByUserId(userId)).thenReturn(List.of());
        when(recommendationService.getRecommendationsForUser(userId, null)).thenReturn(null);
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)).thenReturn(Optional.empty());

        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> {
            LearningPath lp = invocation.getArgument(0);
            lp.setId(UUID.randomUUID());
            return lp;
        });

        when(learningPathItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(difficultyService.determineDifficulty(any(), any(), any())).thenReturn(CourseDifficulty.INTERMEDIATE);

        LearningPathFullResponse response = engineService.generatePath(userId, null, "Test generation");

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTargetCareer());
        assertEquals(1, response.getVersion());
        assertTrue(response.getQualityScore() >= 90.0);
        assertFalse(response.getNodes().isEmpty());

        // Check that Arrays node comes before Binary Search and Trees
        int arraysIdx = -1;
        int binarySearchIdx = -1;
        int treesIdx = -1;

        for (int i = 0; i < response.getNodes().size(); i++) {
            String title = response.getNodes().get(i).getTitle();
            if (title != null) {
                if (title.contains("Arrays")) arraysIdx = i;
                if (title.contains("Binary Search")) binarySearchIdx = i;
                if (title.contains("Trees")) treesIdx = i;
            }
        }

        assertTrue(arraysIdx < binarySearchIdx, "Arrays must precede Binary Search");
        assertTrue(binarySearchIdx < treesIdx, "Binary Search must precede Trees");
    }
}
