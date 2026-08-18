package com.learningpath.learningpath.controller;

import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.LearningPathSummaryResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import com.learningpath.learningpath.service.LearningPathPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserLearningPathControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LearningPathPersistenceService persistenceService;

    @InjectMocks
    private UserLearningPathController controller;

    private UUID userId;
    private UUID pathId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        pathId = UUID.randomUUID();
    }

    @Test
    void getActiveLearningPath_returns200_whenActivePathExists() throws Exception {
        RecommendedCourseItem c1 = new RecommendedCourseItem(UUID.randomUUID(), "Java Basics", "Coursera", 4.8, "BEGINNER", List.of("Java"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1: Foundations", List.of("Java"), List.of(c1), "2 weeks", "Core Java");

        ActiveLearningPathResponse response = new ActiveLearningPathResponse(
                pathId,
                userId,
                "Backend Developer",
                "Personalized Learning Path for Backend Developer",
                "Custom path description",
                LearningPathStatus.ACTIVE,
                1,
                1,
                List.of(phase),
                Instant.now(),
                Instant.now()
        );

        when(persistenceService.getActivePath(userId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{userId}/learning-paths/active", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pathId").value(pathId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.targetCareer").value("Backend Developer"))
                .andExpect(jsonPath("$.totalPhases").value(1))
                .andExpect(jsonPath("$.totalCourses").value(1))
                .andExpect(jsonPath("$.phases[0].phaseTitle").value("Phase 1: Foundations"));
    }

    @Test
    void getActiveLearningPath_returns404_whenNoActivePathFound() throws Exception {
        when(persistenceService.getActivePath(userId))
                .thenThrow(new ResourceNotFoundException("No active learning path found for user id: " + userId));

        mockMvc.perform(get("/api/users/{userId}/learning-paths/active", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLearningPathHistory_returns200_withPathList() throws Exception {
        LearningPathSummaryResponse s1 = new LearningPathSummaryResponse(
                UUID.randomUUID(), userId, "Backend Developer", "Active Path", "desc",
                LearningPathStatus.ACTIVE, 2, 4, Instant.now(), Instant.now()
        );
        LearningPathSummaryResponse s2 = new LearningPathSummaryResponse(
                UUID.randomUUID(), userId, "Data Scientist", "Archived Path", "desc",
                LearningPathStatus.ARCHIVED, 1, 2, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(3600)
        );

        when(persistenceService.getPathHistory(userId)).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/users/{userId}/learning-paths", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Active Path"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].title").value("Archived Path"))
                .andExpect(jsonPath("$[1].status").value("ARCHIVED"));
    }
}
