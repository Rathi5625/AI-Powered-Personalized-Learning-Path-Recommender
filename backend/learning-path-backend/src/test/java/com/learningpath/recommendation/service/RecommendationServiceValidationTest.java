package com.learningpath.recommendation.service;

import com.learningpath.entity.Career;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.ai.reasoning.service.GeminiReasoningService;
import com.learningpath.repository.CourseRepository;
import com.learningpath.skilldependency.dto.LearningOrderResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecommendationServiceValidationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkillGapService skillGapService;

    @Mock
    private CourseSkillRepository courseSkillRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MlRecommendationClient mlRecommendationClient;

    @Mock
    private GeminiReasoningService geminiReasoningService;

    @Mock
    private SkillDependencyService skillDependencyService;

    @InjectMocks
    private RecommendationService recommendationService;

    private User user;
    private Career career;
    private UUID userId;
    private UUID careerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();

        user = User.builder()
                .fullName("Alice Test")
                .experienceLevel(ExperienceLevel.BEGINNER)
                .build();
        user.setId(userId);

        career = Career.builder()
                .title("Java Backend Developer")
                .build();
        career.setId(careerId);

        when(skillDependencyService.getLearningOrder(any())).thenReturn(LearningOrderResponse.ok(Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void testMlFeatureGenerationAndClientInvocation() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Skill javaSkill = Skill.builder().name("Java").build();
        javaSkill.setId(UUID.randomUUID());

        SkillGapItemResponse gap = new SkillGapItemResponse(
                javaSkill.getId(), "Java", "Backend", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.CRITICAL, SkillPriority.CRITICAL, true, "Critical gap"
        );

        SkillGapAnalysisResponse gapAnalysis = new SkillGapAnalysisResponse(
                userId, "Alice Test", careerId, "Java Backend Developer", 1, 0, 0, 1, 1.0, List.of(gap)
        );
        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapAnalysis);

        Course course = Course.builder()
                .title("Java Fundamentals")
                .provider("Udemy")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.VIDEO_COURSE)
                .rating(new BigDecimal("4.80"))
                .isFree(true)
                .build();
        course.setId(UUID.randomUUID());

        CourseSkill cs = CourseSkill.builder().course(course).skill(javaSkill).build();
        when(courseSkillRepository.findBySkillIdIn(any())).thenReturn(List.of(cs));

        MlPredictionResponse mlResponse = new MlPredictionResponse(0.92, 92.0, true, "1.0");
        when(mlRecommendationClient.predict(any(MlPredictionRequest.class))).thenReturn(Optional.of(mlResponse));

        RecommendationSummaryResponse summary = recommendationService.getRecommendationsForUser(userId, careerId);

        assertNotNull(summary);
        assertEquals(1, summary.recommendations().size());

        CourseRecommendationResponse rec = summary.recommendations().get(0);
        assertNotNull(rec.ruleBasedScore());
        assertEquals(92.0, rec.mlScore());
        assertTrue(rec.finalScore() > 0.0);

        ArgumentCaptor<MlPredictionRequest> captor = ArgumentCaptor.forClass(MlPredictionRequest.class);
        verify(mlRecommendationClient, times(1)).predict(captor.capture());

        MlPredictionRequest sentRequest = captor.getValue();
        assertNotNull(sentRequest);
        assertTrue(sentRequest.skillGapScore() >= 0.0 && sentRequest.skillGapScore() <= 1.0);
        assertTrue(sentRequest.mandatorySkillMatch() >= 0.0 && sentRequest.mandatorySkillMatch() <= 1.0);
    }

    @Test
    void testMlServiceFailureFallback() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Skill javaSkill = Skill.builder().name("Java").build();
        javaSkill.setId(UUID.randomUUID());

        SkillGapItemResponse gap = new SkillGapItemResponse(
                javaSkill.getId(), "Java", "Backend", "NONE", ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP, GapSeverity.CRITICAL, SkillPriority.CRITICAL, true, "Critical gap"
        );

        SkillGapAnalysisResponse gapAnalysis = new SkillGapAnalysisResponse(
                userId, "Alice Test", careerId, "Java Backend Developer", 1, 0, 0, 1, 1.0, List.of(gap)
        );
        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapAnalysis);

        Course course = Course.builder()
                .title("Java Fundamentals")
                .provider("Udemy")
                .difficulty(CourseDifficulty.BEGINNER)
                .courseType(CourseType.VIDEO_COURSE)
                .rating(new BigDecimal("4.80"))
                .isFree(true)
                .build();
        course.setId(UUID.randomUUID());

        CourseSkill cs = CourseSkill.builder().course(course).skill(javaSkill).build();
        when(courseSkillRepository.findBySkillIdIn(any())).thenReturn(List.of(cs));

        // Simulate ML Service offline / fallback
        when(mlRecommendationClient.predict(any())).thenReturn(Optional.empty());

        RecommendationSummaryResponse summary = recommendationService.getRecommendationsForUser(userId, careerId);

        assertNotNull(summary);
        assertEquals(1, summary.recommendations().size());

        CourseRecommendationResponse rec = summary.recommendations().get(0);
        assertNotNull(rec.ruleBasedScore());
        assertNull(rec.mlScore(), "mlScore must be null when ML service is offline");
        assertEquals(rec.ruleBasedScore(), rec.finalScore(), "finalScore must equal ruleBasedScore on fallback");
    }

    @Test
    void testNonExistentUserValidation() {
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recommendationService.getRecommendationsForUser(nonExistentUserId, careerId)
        );
    }

    @Test
    void testEmptyCandidateCoursesHandling() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        SkillGapAnalysisResponse gapAnalysis = new SkillGapAnalysisResponse(
                userId, "Alice Test", careerId, "Java Backend Developer", 0, 0, 0, 0, 0.0, Collections.emptyList()
        );
        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(gapAnalysis);
        when(courseSkillRepository.findAll()).thenReturn(Collections.emptyList());

        RecommendationSummaryResponse summary = recommendationService.getRecommendationsForUser(userId, careerId);

        assertNotNull(summary);
        assertTrue(summary.recommendations().isEmpty(), "Recommendations list should be empty when no candidate courses exist");
        assertEquals(0, summary.totalCandidateCourses());
    }

    @Test
    void testMultiLearnerCareerPersonalization() {
        UUID javaUserId = UUID.randomUUID();
        User javaUser = User.builder().fullName("Java Learner").experienceLevel(ExperienceLevel.BEGINNER).build();
        javaUser.setId(javaUserId);

        UUID reactUserId = UUID.randomUUID();
        User reactUser = User.builder().fullName("Frontend Learner").experienceLevel(ExperienceLevel.BEGINNER).build();
        reactUser.setId(reactUserId);

        Skill javaSkill = Skill.builder().name("Java").build(); javaSkill.setId(UUID.randomUUID());
        Skill reactSkill = Skill.builder().name("React").build(); reactSkill.setId(UUID.randomUUID());

        Course javaCourse = Course.builder().title("Java Masterclass").difficulty(CourseDifficulty.BEGINNER).rating(new BigDecimal("4.80")).build();
        javaCourse.setId(UUID.randomUUID());

        Course reactCourse = Course.builder().title("React Complete Guide").difficulty(CourseDifficulty.BEGINNER).rating(new BigDecimal("4.80")).build();
        reactCourse.setId(UUID.randomUUID());

        CourseSkill csJava = CourseSkill.builder().course(javaCourse).skill(javaSkill).build();
        CourseSkill csReact = CourseSkill.builder().course(reactCourse).skill(reactSkill).build();

        // Mock Java Learner
        when(userRepository.findById(javaUserId)).thenReturn(Optional.of(javaUser));
        SkillGapItemResponse javaGap = new SkillGapItemResponse(javaSkill.getId(), "Java", "Backend", "NONE", ProficiencyLevel.BEGINNER, GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, "");
        when(skillGapService.analyzeSkillGap(javaUserId, careerId)).thenReturn(new SkillGapAnalysisResponse(javaUserId, "Java Learner", careerId, "Java Developer", 1, 0, 0, 1, 1.0, List.of(javaGap)));
        when(courseSkillRepository.findBySkillIdIn(Set.of(javaSkill.getId()))).thenReturn(List.of(csJava));
        when(mlRecommendationClient.predict(any())).thenReturn(Optional.of(new MlPredictionResponse(0.90, 90.0, true, "1.0")));

        RecommendationSummaryResponse javaSummary = recommendationService.getRecommendationsForUser(javaUserId, careerId);

        // Mock React Learner
        UUID frontendCareerId = UUID.randomUUID();
        when(userRepository.findById(reactUserId)).thenReturn(Optional.of(reactUser));
        SkillGapItemResponse reactGap = new SkillGapItemResponse(reactSkill.getId(), "React", "Frontend", "NONE", ProficiencyLevel.BEGINNER, GapType.FULL_GAP, GapSeverity.HIGH, SkillPriority.HIGH, false, "");
        when(skillGapService.analyzeSkillGap(reactUserId, frontendCareerId)).thenReturn(new SkillGapAnalysisResponse(reactUserId, "Frontend Learner", frontendCareerId, "Frontend Developer", 1, 0, 0, 1, 1.0, List.of(reactGap)));
        when(courseSkillRepository.findBySkillIdIn(Set.of(reactSkill.getId()))).thenReturn(List.of(csReact));

        RecommendationSummaryResponse reactSummary = recommendationService.getRecommendationsForUser(reactUserId, frontendCareerId);

        assertNotEquals(javaSummary.recommendations().get(0).courseTitle(), reactSummary.recommendations().get(0).courseTitle(),
                "Java learner and React learner must receive personalized, distinct recommendation tracks.");
        assertEquals("Java Masterclass", javaSummary.recommendations().get(0).courseTitle());
        assertEquals("React Complete Guide", reactSummary.recommendations().get(0).courseTitle());
    }
}
