package com.learningpath.learningpath.service;

import com.learningpath.learningpath.dto.AdaptLearningPathResponse;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptiveLearningPathServiceTest {

    @Mock private LearnerStateService learnerStateService;
    @Mock private PathChangeDetector pathChangeDetector;
    @Mock private PersonalizedLearningPathService learningPathService;
    @Mock private LearningPathPersistenceService persistenceService;

    private AdaptiveLearningPathService adaptiveService;

    private UUID userId;
    private UUID careerId;
    private LearnerSnapshot mockSnapshot;

    @BeforeEach
    void setUp() {
        adaptiveService = new AdaptiveLearningPathService(
                learnerStateService,
                pathChangeDetector,
                learningPathService,
                persistenceService
        );

        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();

        mockSnapshot = new LearnerSnapshot(
                userId,
                careerId,
                "Backend Developer",
                Set.of("Java"),
                Set.of("Spring Boot"),
                Collections.emptyList()
        );
    }

    @Test
    void adapt_returnsUnchanged_whenDetectorReportsNoChange() {
        when(learnerStateService.snapshot(userId, careerId)).thenReturn(mockSnapshot);

        RecommendedCourseItem c1 = new RecommendedCourseItem(UUID.randomUUID(), "Java Basics", "Coursera", 4.8, "BEGINNER", List.of("Java"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("Java"), List.of(c1), "2 weeks", "Core");
        PersonalizedLearningPathResponse persistedPath = PersonalizedLearningPathResponse.ok(
                userId, "Backend Developer", "Active summary", List.of(phase), "PERSISTED_DB", "Active Database Path"
        );

        when(persistenceService.getActivePathAsPersonalizedResponse(userId)).thenReturn(Optional.of(persistedPath));
        when(pathChangeDetector.detect(eq(mockSnapshot), eq(persistedPath))).thenReturn(PathChangeDecision.noChange());

        AdaptLearningPathResponse response = adaptiveService.adapt(userId, careerId);

        assertThat(response).isNotNull();
        assertThat(response.adapted()).isFalse();
        assertThat(response.changeReason()).containsIgnoringCase("No meaningful learner state change");
        assertThat(response.path()).isEqualTo(persistedPath);

        // Verify Gemini/generator was NOT invoked
        verify(learningPathService, never()).generateLearningPath(any(), any());
    }

    @Test
    void adapt_triggersRegeneration_whenDetectorReportsChange() {
        when(learnerStateService.snapshot(userId, careerId)).thenReturn(mockSnapshot);

        RecommendedCourseItem c1 = new RecommendedCourseItem(UUID.randomUUID(), "Java Basics", "Coursera", 4.8, "BEGINNER", List.of("Java"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("Java"), List.of(c1), "2 weeks", "Core");
        PersonalizedLearningPathResponse oldPath = PersonalizedLearningPathResponse.ok(
                userId, "Backend Developer", "Old summary", List.of(phase), "PERSISTED_DB", "Active Database Path"
        );

        when(persistenceService.getActivePathAsPersonalizedResponse(userId)).thenReturn(Optional.of(oldPath));
        when(pathChangeDetector.detect(eq(mockSnapshot), eq(oldPath)))
                .thenReturn(PathChangeDecision.adapt("Course completed in active path."));

        PersonalizedLearningPathResponse newGeneratedPath = PersonalizedLearningPathResponse.ok(
                userId, "Backend Developer", "New updated summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );
        when(learningPathService.generateLearningPath(userId, careerId)).thenReturn(newGeneratedPath);

        AdaptLearningPathResponse response = adaptiveService.adapt(userId, careerId);

        assertThat(response).isNotNull();
        assertThat(response.adapted()).isTrue();
        assertThat(response.changeReason()).isEqualTo("Course completed in active path.");
        assertThat(response.path()).isEqualTo(newGeneratedPath);

        // Verify generator was invoked
        verify(learningPathService).generateLearningPath(userId, careerId);
    }

    @Test
    void adapt_triggersInitialGeneration_whenNoActivePathExists() {
        when(learnerStateService.snapshot(userId, careerId)).thenReturn(mockSnapshot);
        when(persistenceService.getActivePathAsPersonalizedResponse(userId)).thenReturn(Optional.empty());
        when(pathChangeDetector.detect(eq(mockSnapshot), isNull()))
                .thenReturn(PathChangeDecision.adapt("No existing learning path found. Generating initial personalized path."));

        PersonalizedLearningPathResponse newPath = PersonalizedLearningPathResponse.ok(
                userId, "Backend Developer", "Initial path", List.of(), "GEMINI", "gemini-1.5-flash"
        );
        when(learningPathService.generateLearningPath(userId, careerId)).thenReturn(newPath);

        AdaptLearningPathResponse response = adaptiveService.adapt(userId, careerId);

        assertThat(response.adapted()).isTrue();
        assertThat(response.path()).isEqualTo(newPath);
        verify(learningPathService).generateLearningPath(userId, careerId);
    }
}
