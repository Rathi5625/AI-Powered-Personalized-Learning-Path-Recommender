package com.learningpath.controller;

import com.learningpath.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealthStatus() {
        HealthResponse response = new HealthResponse(
                "UP",
                "AI Personalized Learning Path Recommender API is running"
        );
        return ResponseEntity.ok(response);
    }
}
