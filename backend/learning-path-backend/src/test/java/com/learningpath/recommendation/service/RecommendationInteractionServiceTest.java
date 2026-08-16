package com.learningpath.recommendation.service;

import com.learningpath.entity.Course;
import com.learningpath.entity.RecommendationInteraction;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.RecommendationInteractionType;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.dto.RecordRecommendationInteractionRequest;
import com.learningpath.recommendation.dto.RecommendationInteractionResponse;
import com.learningpath.recommendation.dto.UserInteractionStatsResponse;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.RecommendationInteractionRepository;
import com.learningpath.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecommendationInteractionServiceTest {

    @Mock
    private RecommendationInteractionRepository interactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private RecommendationInteractionService interactionService;

    private User user;
    private Course course;
    private UUID userId;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        user = User.builder().fullName("Alice Test").build();
        user.setId(userId);

        course = Course.builder().title("Spring Boot Masterclass").build();
        course.setId(courseId);
    }

    @Test
    void testRecordInteractionSuccess() {
        RecordRecommendationInteractionRequest request = new RecordRecommendationInteractionRequest(
                userId, courseId, RecommendationInteractionType.CLICKED, 1, 87.4, 92.1, 89.28
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        RecommendationInteraction savedEntity = RecommendationInteraction.builder()
                .user(user)
                .course(course)
                .interactionType(RecommendationInteractionType.CLICKED)
                .recommendationRank(1)
                .ruleBasedScore(87.4)
                .mlScore(92.1)
                .finalScore(89.28)
                .build();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setCreatedAt(Instant.now());

        when(interactionRepository.save(any(RecommendationInteraction.class))).thenReturn(savedEntity);

        RecommendationInteractionResponse response = interactionService.recordInteraction(request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(courseId, response.courseId());
        assertEquals(RecommendationInteractionType.CLICKED, response.interactionType());
        assertEquals(87.4, response.ruleBasedScore());
        assertEquals(92.1, response.mlScore());
        assertEquals(89.28, response.finalScore());
    }

    @Test
    void testRecordInteractionWithNullMlScoreSuccess() {
        RecordRecommendationInteractionRequest request = new RecordRecommendationInteractionRequest(
                userId, courseId, RecommendationInteractionType.VIEWED, 2, 85.0, null, 85.0
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        RecommendationInteraction savedEntity = RecommendationInteraction.builder()
                .user(user)
                .course(course)
                .interactionType(RecommendationInteractionType.VIEWED)
                .recommendationRank(2)
                .ruleBasedScore(85.0)
                .mlScore(null)
                .finalScore(85.0)
                .build();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setCreatedAt(Instant.now());

        when(interactionRepository.save(any(RecommendationInteraction.class))).thenReturn(savedEntity);

        RecommendationInteractionResponse response = interactionService.recordInteraction(request);

        assertNotNull(response);
        assertNull(response.mlScore());
        assertEquals(85.0, response.finalScore());
    }

    @Test
    void testRecordInteractionInvalidUserThrowsException() {
        UUID nonExistentUserId = UUID.randomUUID();
        RecordRecommendationInteractionRequest request = new RecordRecommendationInteractionRequest(
                nonExistentUserId, courseId, RecommendationInteractionType.CLICKED, 1, 80.0, 90.0, 84.0
        );

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> interactionService.recordInteraction(request));
    }

    @Test
    void testRecordInteractionInvalidCourseThrowsException() {
        UUID nonExistentCourseId = UUID.randomUUID();
        RecordRecommendationInteractionRequest request = new RecordRecommendationInteractionRequest(
                userId, nonExistentCourseId, RecommendationInteractionType.CLICKED, 1, 80.0, 90.0, 84.0
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(nonExistentCourseId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> interactionService.recordInteraction(request));
    }

    @Test
    void testGetUserInteractionStats() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(interactionRepository.countByUserId(userId)).thenReturn(10L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.VIEWED)).thenReturn(4L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.CLICKED)).thenReturn(3L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.STARTED)).thenReturn(1L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.COMPLETED)).thenReturn(1L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.LIKED)).thenReturn(1L);
        when(interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.SKIPPED)).thenReturn(0L);

        UserInteractionStatsResponse stats = interactionService.getUserInteractionStats(userId);

        assertNotNull(stats);
        assertEquals(10L, stats.totalInteractions());
        assertEquals(4L, stats.viewed());
        assertEquals(3L, stats.clicked());
        assertEquals(1L, stats.started());
        assertEquals(1L, stats.completed());
        assertEquals(1L, stats.liked());
        assertEquals(0L, stats.skipped());
    }
}
