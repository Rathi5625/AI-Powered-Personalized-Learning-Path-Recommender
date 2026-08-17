package com.learningpath.learningpath.service;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.*;
import com.learningpath.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathPersistenceServiceTest {

    @Mock private LearningPathRepository learningPathRepository;
    @Mock private LearningPathItemRepository learningPathItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSkillRepository courseSkillRepository;
    @Mock private UserProgressRepository userProgressRepository;

    private LearningPathPersistenceService persistenceService;

    private UUID userId;
    private UUID careerId;
    private UUID courseId1;
    private UUID courseId2;
    private User testUser;
    private Career testCareer;
    private Course testCourse1;
    private Course testCourse2;

    @BeforeEach
    void setUp() {
        persistenceService = new LearningPathPersistenceService(
                learningPathRepository,
                learningPathItemRepository,
                userRepository,
                careerRepository,
                courseRepository,
                courseSkillRepository,
                userProgressRepository
        );

        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();
        courseId1 = UUID.randomUUID();
        courseId2 = UUID.randomUUID();

        testUser = User.builder().fullName("Test Learner").email("test@example.com").build();
        setId(testUser, userId);
        testCareer = Career.builder().title("Backend Developer").build();
        setId(testCareer, careerId);
        testCourse1 = Course.builder().title("Java Basics").provider("Coursera").difficulty(CourseDifficulty.BEGINNER).build();
        setId(testCourse1, courseId1);
        testCourse2 = Course.builder().title("Spring Boot Masterclass").provider("Udemy").difficulty(CourseDifficulty.INTERMEDIATE).build();
        setId(testCourse2, courseId2);
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = com.learningpath.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID: " + e.getMessage(), e);
        }
    }

    @Test
    void saveLearningPath_archivesExistingPath_andPersistsNewActivePath() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(careerRepository.findById(careerId)).thenReturn(Optional.of(testCareer));
        when(learningPathRepository.updateStatusByUserIdAndStatus(userId, LearningPathStatus.ACTIVE, LearningPathStatus.ARCHIVED)).thenReturn(1);

        LearningPath savedEntity = LearningPath.builder()
                .user(testUser)
                .targetCareer(testCareer)
                .title("Personalized Learning Path for Backend Developer")
                .description("Test path summary")
                .status(LearningPathStatus.ACTIVE)
                .build();
        when(learningPathRepository.save(any(LearningPath.class))).thenReturn(savedEntity);

        when(courseRepository.findById(courseId1)).thenReturn(Optional.of(testCourse1));
        when(courseRepository.findById(courseId2)).thenReturn(Optional.of(testCourse2));
        when(userProgressRepository.findByUserIdAndCourseId(userId, courseId1)).thenReturn(Optional.empty());
        when(userProgressRepository.findByUserIdAndCourseId(userId, courseId2)).thenReturn(Optional.empty());

        RecommendedCourseItem c1 = new RecommendedCourseItem(courseId1, "Java Basics", "Coursera", 4.8, "BEGINNER", List.of("Java"));
        RecommendedCourseItem c2 = new RecommendedCourseItem(courseId2, "Spring Boot Masterclass", "Udemy", 4.9, "INTERMEDIATE", List.of("Spring Boot"));

        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1: Foundations", List.of("Java"), List.of(c1), "2 weeks", "Core Java");
        LearningPathPhase phase2 = new LearningPathPhase(2, "Phase 2: Frameworks", List.of("Spring Boot"), List.of(c2), "3 weeks", "Spring Framework");

        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Backend Developer", "Test path summary", List.of(phase1, phase2), "GEMINI", "gemini-1.5-flash"
        );

        LearningPath result = persistenceService.saveLearningPath(userId, careerId, response);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LearningPathStatus.ACTIVE);

        // Verify old path was archived
        verify(learningPathRepository).updateStatusByUserIdAndStatus(userId, LearningPathStatus.ACTIVE, LearningPathStatus.ARCHIVED);

        // Verify items were persisted
        verify(learningPathItemRepository, times(2)).save(any(LearningPathItem.class));
    }

    @Test
    void getActivePath_returnsActivePath_whenExists() {
        when(userRepository.existsById(userId)).thenReturn(true);

        LearningPath activePath = LearningPath.builder()
                .user(testUser)
                .targetCareer(testCareer)
                .title("Personalized Learning Path for Backend Developer")
                .description("Active path description")
                .status(LearningPathStatus.ACTIVE)
                .build();
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)).thenReturn(Optional.of(activePath));

        LearningPathItem item1 = LearningPathItem.builder()
                .learningPath(activePath)
                .course(testCourse1)
                .phaseNumber(1)
                .phaseTitle("Phase 1: Foundations")
                .estimatedDuration("2 weeks")
                .explanation("Core Java")
                .itemOrder(1)
                .isCompleted(false)
                .build();

        when(learningPathItemRepository.findByLearningPathIdOrderByPhaseNumberAscItemOrderAsc(activePath.getId()))
                .thenReturn(List.of(item1));
        when(courseSkillRepository.findByCourseId(testCourse1.getId())).thenReturn(List.of());

        ActiveLearningPathResponse response = persistenceService.getActivePath(userId);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(LearningPathStatus.ACTIVE);
        assertThat(response.targetCareer()).isEqualTo("Backend Developer");
        assertThat(response.phases()).hasSize(1);
        assertThat(response.totalCourses()).isEqualTo(1);
    }

    @Test
    void getActivePath_throwsResourceNotFoundException_whenNoActivePath() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> persistenceService.getActivePath(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No active learning path found");
    }

    @Test
    void getActivePathAsPersonalizedResponse_returnsEmpty_whenNoActivePath() {
        when(learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)).thenReturn(Optional.empty());

        Optional<PersonalizedLearningPathResponse> result = persistenceService.getActivePathAsPersonalizedResponse(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getPathHistory_returnsAllHistoricalPaths() {
        when(userRepository.existsById(userId)).thenReturn(true);

        LearningPath p1 = LearningPath.builder()
                .user(testUser)
                .targetCareer(testCareer)
                .title("Active Path")
                .status(LearningPathStatus.ACTIVE)
                .build();

        LearningPath p2 = LearningPath.builder()
                .user(testUser)
                .targetCareer(testCareer)
                .title("Old Archived Path")
                .status(LearningPathStatus.ARCHIVED)
                .build();

        when(learningPathRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(p1, p2));
        when(learningPathItemRepository.findByLearningPathIdOrderByPhaseNumberAscItemOrderAsc(any())).thenReturn(List.of());

        List<LearningPathSummaryResponse> history = persistenceService.getPathHistory(userId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).title()).isEqualTo("Active Path");
        assertThat(history.get(1).title()).isEqualTo("Old Archived Path");
    }

    @Test
    void getPathHistory_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> persistenceService.getPathHistory(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
