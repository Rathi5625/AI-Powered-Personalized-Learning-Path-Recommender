package com.learningpath.controller;

import com.learningpath.dto.LearningProgressRequest;
import com.learningpath.dto.LearningProgressResponse;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.service.UserProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProgressControllerTest {

        private MockMvc mockMvc;

        @Mock
        private UserProgressService userProgressService;

        @InjectMocks
        private UserProgressController controller;

        private UUID userId;
        private UUID courseId;
        private UUID progressId;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                userId = UUID.randomUUID();
                courseId = UUID.randomUUID();
                progressId = UUID.randomUUID();
        }

        @Test
        void putProgress_returns200_withValidRequest() throws Exception {
                LearningProgressResponse mockResponse = new LearningProgressResponse(
                                progressId, userId, courseId, "Java Fundamentals",
                                ProgressStatus.IN_PROGRESS, new BigDecimal("60.00"),
                                Instant.now(), Instant.now());

                when(userProgressService.upsertProgress(eq(userId), eq(courseId), any(LearningProgressRequest.class)))
                                .thenReturn(mockResponse);

                String requestJson = """
                                {
                                    "status": "IN_PROGRESS",
                                    "completionPercentage": 60.00
                                }
                                """;

                mockMvc.perform(put("/api/users/{userId}/learning-progress/{courseId}", userId, courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.progressId").value(progressId.toString()))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                                .andExpect(jsonPath("$.completionPercentage").value(60.00));
        }

        @Test
        void putProgress_returns200_withCompletedStatus() throws Exception {
                LearningProgressResponse mockResponse = new LearningProgressResponse(
                                progressId, userId, courseId, "Spring Boot",
                                ProgressStatus.COMPLETED, new BigDecimal("100.00"),
                                Instant.now(), Instant.now());

                when(userProgressService.upsertProgress(eq(userId), eq(courseId), any(LearningProgressRequest.class)))
                                .thenReturn(mockResponse);

                String requestJson = """
                                {
                                    "status": "COMPLETED",
                                    "completionPercentage": 100.00
                                }
                                """;

                mockMvc.perform(put("/api/users/{userId}/learning-progress/{courseId}", userId, courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.completionPercentage").value(100.00));
        }

        @Test
        void putProgress_returns400_whenStatusIsMissing() throws Exception {
                String requestJson = """
                                {
                                    "completionPercentage": 50.00
                                }
                                """;

                mockMvc.perform(put("/api/users/{userId}/learning-progress/{courseId}", userId, courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void putProgress_returns404_whenUserNotFound() throws Exception {
                when(userProgressService.upsertProgress(eq(userId), eq(courseId), any(LearningProgressRequest.class)))
                                .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));

                String requestJson = """
                                {
                                    "status": "IN_PROGRESS",
                                    "completionPercentage": 50.00
                                }
                                """;

                mockMvc.perform(put("/api/users/{userId}/learning-progress/{courseId}", userId, courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getProgress_returnsAllProgressForUser() throws Exception {
                LearningProgressResponse r1 = new LearningProgressResponse(
                                UUID.randomUUID(), userId, UUID.randomUUID(), "Course A",
                                ProgressStatus.COMPLETED, BigDecimal.valueOf(100), Instant.now(), Instant.now());
                LearningProgressResponse r2 = new LearningProgressResponse(
                                UUID.randomUUID(), userId, UUID.randomUUID(), "Course B",
                                ProgressStatus.IN_PROGRESS, BigDecimal.valueOf(40), Instant.now(), Instant.now());

                when(userProgressService.getUserProgress(userId)).thenReturn(List.of(r1, r2));

                mockMvc.perform(get("/api/users/{userId}/learning-progress", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getCourseProgress_returnsSingleRecord() throws Exception {
                LearningProgressResponse response = new LearningProgressResponse(
                                progressId, userId, courseId, "Docker",
                                ProgressStatus.IN_PROGRESS, BigDecimal.valueOf(55), Instant.now(), Instant.now());

                when(userProgressService.getCourseProgress(userId, courseId)).thenReturn(response);

                mockMvc.perform(get("/api/users/{userId}/learning-progress/{courseId}", userId, courseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.courseTitle").value("Docker"))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        void getCourseProgress_returns404_whenNotFound() throws Exception {
                when(userProgressService.getCourseProgress(userId, courseId))
                                .thenThrow(new ResourceNotFoundException("No progress record found"));

                mockMvc.perform(get("/api/users/{userId}/learning-progress/{courseId}", userId, courseId))
                                .andExpect(status().isNotFound());
        }
}
