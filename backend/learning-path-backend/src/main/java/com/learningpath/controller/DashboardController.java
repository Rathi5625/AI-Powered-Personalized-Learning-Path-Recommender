package com.learningpath.controller;

import com.learningpath.dto.DashboardAggregatedResponse;
import com.learningpath.dto.DashboardResponse;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.DashboardAggregationService;
import com.learningpath.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAggregationService dashboardAggregationService;
    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard")
    public ResponseEntity<DashboardAggregatedResponse> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(dashboardAggregationService.getDashboardData(principal.getId()));
    }

    @GetMapping("/api/dashboard/users/{userId}")
    public ResponseEntity<DashboardAggregatedResponse> getDashboardByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(dashboardAggregationService.getDashboardData(userId));
    }

    @GetMapping("/api/users/{userId}/dashboard")
    public ResponseEntity<DashboardResponse> getUserDashboard(@PathVariable UUID userId) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
