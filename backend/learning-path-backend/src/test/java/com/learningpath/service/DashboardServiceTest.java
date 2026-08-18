package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.Career;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.learningpath.service.LearningPathPersistenceService;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.repository.CareerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private LearningPathPersistenceService learningPathPersistenceService;
    @Mock
    private UserProgressService userProgressService;
    @Mock
    private SkillGapService skillGapService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private CareerRepository careerRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID userId;
    private UUID careerId;
    private UserResponse mockUser;
    private Career mockCareer;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();

        mockUser = new UserResponse(
                userId,
                "Parth",
                "parth@example.com",
                "Frontend Developer",
                null,
                2,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        mockCareer = Career.builder()
                .title("Frontend Developer")
                .description("Build web apps")
                .build();
    }

    @Test
    @DisplayName("1. Successful dashboard retrieval with all data populated")
    void testGetDashboard_Success() {
        when(userService.getUserById(userId)).thenReturn(mockUser);

        ActiveLearningPathResponse activePath = new ActiveLearningPathResponse(
                UUID.randomUUID(),
                userId,
                "Frontend Developer",
                "Personalized Roadmap",
                "Summary",
                null,
                2,
                4,
                List.of(),
                Instant.now(),
                Instant.now()
        );
        when(learningPathPersistenceService.getActivePath(userId)).thenReturn(activePath);

        UserProgressSummaryResponse progressSummary = new UserProgressSummaryResponse(4, 2, 1, 1, 0, 50.0);
        when(userProgressService.getProgressSummary(userId)).thenReturn(progressSummary);

        when(careerRepository.findByTitle("Frontend Developer")).thenReturn(Optional.of(mockCareer));

        SkillGapAnalysisResponse gapAnalysis = new SkillGapAnalysisResponse(
                userId, "Parth", careerId, "Frontend Developer", 8, 4, 2, 2, 0.4, List.of()
        );
        when(skillGapService.analyzeSkillGap(eq(userId), any())).thenReturn(gapAnalysis);

        CourseRecommendationResponse rec1 = new CourseRecommendationResponse(
                1,
                UUID.randomUUID(),
                "React Masterclass",
                "Udemy",
                "https://example.com/react",
                com.learningpath.entity.enums.CourseDifficulty.INTERMEDIATE,
                com.learningpath.entity.enums.CourseType.PROJECT_BASED,
                new java.math.BigDecimal("4.8"),
                new java.math.BigDecimal("49.99"),
                false,
                90.0,
                85.0,
                88.0,
                List.of("React"),
                List.of("React"),
                "High match"
        );
        RecommendationSummaryResponse recSummary = new RecommendationSummaryResponse(
                userId, "Parth", careerId, "Frontend Developer", true, 1, List.of(rec1)
        );
        when(recommendationService.getRecommendationsForUser(eq(userId), any(), eq(5), eq(false), isNull())).thenReturn(recSummary);

        DashboardResponse response = dashboardService.getDashboard(userId);

        assertThat(response).isNotNull();
        assertThat(response.user().name()).isEqualTo("Parth");
        assertThat(response.activeLearningPath()).isNotNull();
        assertThat(response.activeLearningPath().title()).isEqualTo("Personalized Roadmap");
        assertThat(response.progressSummary().totalCoursesTracked()).isEqualTo(4);
        assertThat(response.progressSummary().overallCompletionRate()).isEqualTo(50.0);
        assertThat(response.skillGapSummary()).isNotNull();
        assertThat(response.skillGapSummary().totalRequiredSkills()).isEqualTo(8);
        assertThat(response.skillGapSummary().acquiredSkills()).isEqualTo(4);
        assertThat(response.topRecommendations()).hasSize(1);
        assertThat(response.topRecommendations().get(0).courseTitle()).isEqualTo("React Masterclass");
    }

    @Test
    @DisplayName("2. User not found throws ResourceNotFoundException")
    void testGetDashboard_UserNotFound() {
        when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("User not found"));

        assertThatThrownBy(() -> dashboardService.getDashboard(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("3 & 4. Active learning path not found returns null without crashing")
    void testGetDashboard_NoActiveLearningPath() {
        when(userService.getUserById(userId)).thenReturn(mockUser);
        when(learningPathPersistenceService.getActivePath(userId)).thenThrow(new ResourceNotFoundException("No active path"));
        when(userProgressService.getProgressSummary(userId)).thenReturn(new UserProgressSummaryResponse(0, 0, 0, 0, 0, 0.0));
        when(careerRepository.findByTitle("Frontend Developer")).thenReturn(Optional.empty());

        DashboardResponse response = dashboardService.getDashboard(userId);

        assertThat(response).isNotNull();
        assertThat(response.activeLearningPath()).isNull();
        assertThat(response.progressSummary().totalCoursesTracked()).isEqualTo(0);
        assertThat(response.skillGapSummary()).isNull();
        assertThat(response.topRecommendations()).isEmpty();
    }
}
