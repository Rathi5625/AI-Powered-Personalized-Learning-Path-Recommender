package com.learningpath.recommendation.controller;

import com.learningpath.recommendation.dto.RecordRecommendationInteractionRequest;
import com.learningpath.recommendation.dto.RecommendationInteractionResponse;
import com.learningpath.recommendation.dto.UserInteractionStatsResponse;
import com.learningpath.recommendation.service.RecommendationInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendation-interactions")
@RequiredArgsConstructor
public class RecommendationInteractionController {

    private final RecommendationInteractionService interactionService;

    @PostMapping
    public ResponseEntity<RecommendationInteractionResponse> recordInteraction(
            @Valid @RequestBody RecordRecommendationInteractionRequest request
    ) {
        RecommendationInteractionResponse response = interactionService.recordInteraction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendationInteractionResponse>> getUserInteractions(
            @PathVariable UUID userId
    ) {
        List<RecommendationInteractionResponse> response = interactionService.getUserInteractions(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<RecommendationInteractionResponse>> getCourseInteractions(
            @PathVariable UUID courseId
    ) {
        List<RecommendationInteractionResponse> response = interactionService.getCourseInteractions(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<UserInteractionStatsResponse> getUserInteractionStats(
            @PathVariable UUID userId
    ) {
        UserInteractionStatsResponse stats = interactionService.getUserInteractionStats(userId);
        return ResponseEntity.ok(stats);
    }
}
