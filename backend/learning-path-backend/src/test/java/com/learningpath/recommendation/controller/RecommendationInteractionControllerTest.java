package com.learningpath.recommendation.controller;

import com.learningpath.entity.enums.RecommendationInteractionType;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.recommendation.dto.RecommendationInteractionResponse;
import com.learningpath.recommendation.dto.UserInteractionStatsResponse;
import com.learningpath.recommendation.service.RecommendationInteractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationInteractionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationInteractionService interactionService;

    @InjectMocks
    private RecommendationInteractionController interactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(interactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recordInteractionShouldReturn201Created() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        RecommendationInteractionResponse response = new RecommendationInteractionResponse(
                id, userId, courseId, RecommendationInteractionType.CLICKED, 1, 87.4, 92.1, 89.28, Instant.now()
        );

        when(interactionService.recordInteraction(any())).thenReturn(response);

        String jsonPayload = String.format("""
                {
                  "userId": "%s",
                  "courseId": "%s",
                  "interactionType": "CLICKED",
                  "recommendationRank": 1,
                  "ruleBasedScore": 87.4,
                  "mlScore": 92.1,
                  "finalScore": 89.28
                }
                """, userId, courseId);

        mockMvc.perform(post("/api/recommendation-interactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.interactionType").value("CLICKED"))
                .andExpect(jsonPath("$.finalScore").value(89.28));
    }

    @Test
    void getUserInteractionStatsShouldReturnStats() throws Exception {
        UUID userId = UUID.randomUUID();
        UserInteractionStatsResponse stats = new UserInteractionStatsResponse(10, 4, 3, 1, 1, 1, 0);

        when(interactionService.getUserInteractionStats(userId)).thenReturn(stats);

        mockMvc.perform(get("/api/recommendation-interactions/user/{userId}/stats", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInteractions").value(10))
                .andExpect(jsonPath("$.clicked").value(3))
                .andExpect(jsonPath("$.viewed").value(4));
    }
}
