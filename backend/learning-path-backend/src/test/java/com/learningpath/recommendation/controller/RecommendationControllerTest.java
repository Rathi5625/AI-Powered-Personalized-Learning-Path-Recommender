package com.learningpath.recommendation.controller;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(recommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getRecommendationsShouldReturnSummaryWithRankedCourses() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CourseRecommendationResponse rec = new CourseRecommendationResponse(
                1,
                courseId,
                "Building Production REST APIs with Spring Boot",
                "Udemy",
                "https://example.org/courses/spring-boot-rest-api",
                CourseDifficulty.INTERMEDIATE,
                CourseType.PROJECT_BASED,
                new BigDecimal("4.92"),
                new BigDecimal("69.99"),
                false,
                91.4,
                95.0,
                92.84,
                List.of("Spring Boot", "REST APIs"),
                List.of("Spring Boot", "REST APIs"),
                "Strongly recommended as it directly addresses your critical skill gaps."
        );

        RecommendationSummaryResponse summary = new RecommendationSummaryResponse(
                userId,
                "John Doe",
                careerId,
                "Java Backend Developer",
                true,
                5,
                List.of(rec)
        );

        when(recommendationService.getRecommendationsForUser(eq(userId), eq(careerId), any(), any(), any()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/users/{userId}/recommendations", userId)
                        .param("careerId", careerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.careerName").value("Java Backend Developer"))
                .andExpect(jsonPath("$.hasGaps").value(true))
                .andExpect(jsonPath("$.recommendations[0].rank").value(1))
                .andExpect(jsonPath("$.recommendations[0].courseTitle").value("Building Production REST APIs with Spring Boot"))
                .andExpect(jsonPath("$.recommendations[0].ruleBasedScore").value(91.4))
                .andExpect(jsonPath("$.recommendations[0].mlScore").value(95.0))
                .andExpect(jsonPath("$.recommendations[0].finalScore").value(92.84));
    }

    @Test
    void getRecommendationsWithNonExistentUserShouldReturn404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        when(recommendationService.getRecommendationsForUser(eq(nonExistentUserId), eq(careerId), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + nonExistentUserId));

        mockMvc.perform(get("/api/users/{userId}/recommendations", nonExistentUserId)
                        .param("careerId", careerId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentUserId));
    }

    @Test
    void getRecommendations_withoutCareerId_shouldSucceed() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();

        RecommendationSummaryResponse summary = new RecommendationSummaryResponse(
                userId, "John Doe", careerId, "Java Backend Developer", false, 0, List.of()
        );

        when(recommendationService.getRecommendationsForUser(eq(userId), isNull(), any(), any(), any()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/users/{userId}/recommendations", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.careerName").value("Java Backend Developer"));
    }
}
