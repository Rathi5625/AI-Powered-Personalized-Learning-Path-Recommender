package com.learningpath.recommendation.controller;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<RecommendationSummaryResponse> getRecommendations(
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID careerId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "false") Boolean freeOnly,
            @RequestParam(required = false) CourseDifficulty difficulty
    ) {
        RecommendationSummaryResponse response = recommendationService.getRecommendationsForUser(
                userId, careerId, limit, freeOnly, difficulty
        );
        return ResponseEntity.ok(response);
    }
}
