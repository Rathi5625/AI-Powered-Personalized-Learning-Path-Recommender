package com.learningpath.learningpath.controller;

import com.learningpath.ai.validation.LearningPathValidator;
import com.learningpath.learningpath.dto.AdaptLearningPathRequest;
import com.learningpath.learningpath.dto.AdaptLearningPathResponse;
import com.learningpath.learningpath.dto.GenerateLearningPathRequest;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.service.AdaptiveLearningPathService;
import com.learningpath.learningpath.service.PersonalizedLearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
@Slf4j
public class PersonalizedLearningPathController {

    private final PersonalizedLearningPathService learningPathService;
    private final LearningPathValidator validator;
    private final AdaptiveLearningPathService adaptiveLearningPathService;

    /**
     * Generate a new personalized learning path from scratch.
     *
     * POST /api/learning-paths/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<PersonalizedLearningPathResponse> generateLearningPath(
            @RequestBody GenerateLearningPathRequest request
    ) {
        if (request == null || request.userId() == null) {
            log.warn("[PersonalizedLearningPathController] Invalid request: missing userId");
            return ResponseEntity.badRequest()
                    .body(PersonalizedLearningPathResponse.fail("User ID is required"));
        }

        log.info("[PersonalizedLearningPathController] Received request to generate path for userId: {}, careerId: {}",
                request.userId(), request.careerId());

        try {
            PersonalizedLearningPathResponse response = learningPathService.generateLearningPath(request.userId(), request.careerId());
            if (response != null && response.success()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
        } catch (Exception e) {
            log.error("[PersonalizedLearningPathController] Service exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(PersonalizedLearningPathResponse.fail("AI Learning Path service unavailable: " + e.getMessage()));
        }
    }

    /**
     * Adaptively update the learner's path based on their current state.
     *
     * POST /api/users/{userId}/learning-path/adapt
     *
     * If the learner's state has not meaningfully changed, the existing path is returned
     * with adapted=false. If a change is detected (course completed, career changed, etc.),
     * the path is regenerated through the full Gemini + validation pipeline.
     */
    @PostMapping("/users/{userId}/adapt")
    public ResponseEntity<AdaptLearningPathResponse> adaptLearningPath(
            @PathVariable UUID userId,
            @Valid @RequestBody AdaptLearningPathRequest request
    ) {
        log.info("[PersonalizedLearningPathController] Adapt request for userId={}, careerId={}", userId, request.careerId());

        try {
            AdaptLearningPathResponse response = adaptiveLearningPathService.adapt(userId, request.careerId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[PersonalizedLearningPathController] Adapt exception for userId={}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
