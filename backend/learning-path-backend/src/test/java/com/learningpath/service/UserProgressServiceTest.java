package com.learningpath.service;

import com.learningpath.dto.LearningProgressRequest;
import com.learningpath.dto.LearningProgressResponse;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.entity.UserProgress;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserProgressRepository progressRepository;

    private UserProgressService service;

    private UUID userId;
    private UUID courseId;
    private User mockUser;
    private Course mockCourse;

    @BeforeEach
    void setUp() {
        service = new UserProgressService(userRepository, courseRepository, progressRepository);

        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        mockUser = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .build();

        mockCourse = Course.builder()
                .title("Java Fundamentals")
                .build();
    }

    @Test
    void upsertProgress_createsNewRecord_whenNoneExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(progressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        UserProgress saved = buildProgress(ProgressStatus.IN_PROGRESS, new BigDecimal("45.00"));
        when(progressRepository.save(any(UserProgress.class))).thenReturn(saved);

        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.IN_PROGRESS, new BigDecimal("45.00"));
        LearningProgressResponse response = service.upsertProgress(userId, courseId, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ProgressStatus.IN_PROGRESS);
        verify(progressRepository).save(any(UserProgress.class));
    }

    @Test
    void upsertProgress_updatesExistingRecord_whenAlreadyExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));

        UserProgress existing = buildProgress(ProgressStatus.IN_PROGRESS, new BigDecimal("30.00"));
        when(progressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.of(existing));

        UserProgress updated = buildProgress(ProgressStatus.IN_PROGRESS, new BigDecimal("70.00"));
        when(progressRepository.save(any(UserProgress.class))).thenReturn(updated);

        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.IN_PROGRESS, new BigDecimal("70.00"));
        LearningProgressResponse response = service.upsertProgress(userId, courseId, request);

        assertThat(response).isNotNull();
        assertThat(response.completionPercentage()).isEqualByComparingTo(new BigDecimal("70.00"));
        verify(progressRepository).save(any(UserProgress.class));
    }

    @Test
    void upsertProgress_setsCompletionTo100_whenStatusIsCompleted() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(progressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        UserProgress saved = buildProgress(ProgressStatus.COMPLETED, new BigDecimal("100.00"));
        when(progressRepository.save(any(UserProgress.class))).thenReturn(saved);

        // Request provides 50% but COMPLETED should override to 100%
        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.COMPLETED, new BigDecimal("50.00"));
        service.upsertProgress(userId, courseId, request);

        // Verify the saved entity has 100% regardless of what was in the request
        verify(progressRepository).save(argThat(p ->
                p.getCompletionPercentage().compareTo(BigDecimal.valueOf(100)) == 0
        ));
    }

    @Test
    void upsertProgress_setsCompletionToZero_whenStatusIsNotStarted() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(progressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        UserProgress saved = buildProgress(ProgressStatus.NOT_STARTED, BigDecimal.ZERO);
        when(progressRepository.save(any(UserProgress.class))).thenReturn(saved);

        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.NOT_STARTED, new BigDecimal("75.00"));
        service.upsertProgress(userId, courseId, request);

        verify(progressRepository).save(argThat(p ->
                p.getCompletionPercentage().compareTo(BigDecimal.ZERO) == 0
        ));
    }

    @Test
    void upsertProgress_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.IN_PROGRESS, new BigDecimal("50.00"));

        assertThatThrownBy(() -> service.upsertProgress(userId, courseId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void upsertProgress_throwsResourceNotFoundException_whenCourseNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.IN_PROGRESS, new BigDecimal("50.00"));

        assertThatThrownBy(() -> service.upsertProgress(userId, courseId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(courseId.toString());
    }

    @Test
    void getUserProgress_returnsAllProgressForUser() {
        when(userRepository.existsById(userId)).thenReturn(true);
        UserProgress p1 = buildProgress(ProgressStatus.COMPLETED, new BigDecimal("100.00"));
        UserProgress p2 = buildProgress(ProgressStatus.IN_PROGRESS, new BigDecimal("40.00"));
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(p1, p2));

        List<LearningProgressResponse> result = service.getUserProgress(userId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getUserProgress_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> service.getUserProgress(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCourseProgress_throwsResourceNotFoundException_whenNoRecord() {
        when(progressRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCourseProgress(userId, courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProgressSummary_returnsCorrectSummary_whenRecordsExist() {
        when(userRepository.existsById(userId)).thenReturn(true);
        UserProgress p1 = buildProgress(ProgressStatus.COMPLETED, new BigDecimal("100.00"));
        UserProgress p2 = buildProgress(ProgressStatus.IN_PROGRESS, new BigDecimal("50.00"));
        UserProgress p3 = buildProgress(ProgressStatus.NOT_STARTED, BigDecimal.ZERO);
        UserProgress p4 = buildProgress(ProgressStatus.PAUSED, new BigDecimal("20.00"));
        when(progressRepository.findByUserId(userId)).thenReturn(List.of(p1, p2, p3, p4));

        com.learningpath.dto.UserProgressSummaryResponse summary = service.getProgressSummary(userId);

        assertThat(summary).isNotNull();
        assertThat(summary.totalCoursesTracked()).isEqualTo(4);
        assertThat(summary.completedCourses()).isEqualTo(1);
        assertThat(summary.inProgressCourses()).isEqualTo(1);
        assertThat(summary.notStartedCourses()).isEqualTo(1);
        assertThat(summary.pausedCourses()).isEqualTo(1);
        assertThat(summary.overallCompletionRate()).isEqualTo(42.5); // (100 + 50 + 0 + 20) / 4 = 42.5
    }

    @Test
    void getProgressSummary_returnsZeroes_whenNoRecordsExist() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(progressRepository.findByUserId(userId)).thenReturn(List.of());

        com.learningpath.dto.UserProgressSummaryResponse summary = service.getProgressSummary(userId);

        assertThat(summary).isNotNull();
        assertThat(summary.totalCoursesTracked()).isEqualTo(0);
        assertThat(summary.completedCourses()).isEqualTo(0);
        assertThat(summary.overallCompletionRate()).isEqualTo(0.0);
    }

    // ---

    private UserProgress buildProgress(ProgressStatus status, BigDecimal pct) {
        UserProgress p = UserProgress.builder()
                .user(mockUser)
                .course(mockCourse)
                .status(status)
                .completionPercentage(pct)
                .lastAccessedAt(Instant.now())
                .build();
        return p;
    }
}
