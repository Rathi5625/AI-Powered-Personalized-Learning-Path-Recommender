package com.learningpath.controller;

import com.learningpath.dto.DashboardResponse;
import com.learningpath.dto.UserProgressSummaryResponse;
import com.learningpath.dto.UserResponse;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/users/{userId}/dashboard returns 200 OK")
    void testGetDashboard_Success() throws Exception {
        UserResponse user = new UserResponse(
                userId, "Parth", "parth@example.com", "Full Stack", null, 2, null, null, Instant.now(), Instant.now()
        );
        UserProgressSummaryResponse progress = new UserProgressSummaryResponse(2, 1, 1, 0, 0, 50.0);
        DashboardResponse dashboard = new DashboardResponse(user, null, progress, null, List.of());

        when(dashboardService.getDashboard(userId)).thenReturn(dashboard);

        mockMvc.perform(get("/api/users/" + userId + "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Parth"))
                .andExpect(jsonPath("$.progressSummary.totalCoursesTracked").value(2))
                .andExpect(jsonPath("$.progressSummary.overallCompletionRate").value(50.0));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/dashboard returns 404 NOT FOUND for non-existent user")
    void testGetDashboard_NotFound() throws Exception {
        when(dashboardService.getDashboard(userId))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/api/users/" + userId + "/dashboard"))
                .andExpect(status().isNotFound());
    }
}
