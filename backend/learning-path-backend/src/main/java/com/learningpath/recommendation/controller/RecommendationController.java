package com.learningpath.recommendation.controller;

import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping("/api/users/{userId}/recommendations")
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

    @GetMapping("/api/recommendations")
    public ResponseEntity<RecommendationSummaryResponse> getCurrentUserRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID careerId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "false") Boolean freeOnly,
            @RequestParam(required = false) CourseDifficulty difficulty
    ) {
        UUID userId;
        if (principal != null) {
            userId = principal.getId();
        } else {
            userId = userRepository.findAll().stream().findFirst().map(User::getId).orElse(UUID.randomUUID());
        }

        RecommendationSummaryResponse response = recommendationService.getRecommendationsForUser(
                userId, careerId, limit, freeOnly, difficulty
        );
        return ResponseEntity.ok(response);
    }
}
