package com.learningpath.learningpath.controller;

import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.learningpath.dto.LearningPathSummaryResponse;
import com.learningpath.learningpath.service.LearningPathPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller exposing REST endpoints for managing and retrieving persisted learner learning paths.
 *
 * Base Path: /api/users/{userId}/learning-paths
 */
@RestController
@RequestMapping("/api/users/{userId}/learning-paths")
@RequiredArgsConstructor
@Slf4j
public class UserLearningPathController {

    private final LearningPathPersistenceService persistenceService;

    /**
     * Retrieves the learner's active learning path.
     *
     * GET /api/users/{userId}/learning-paths/active
     */
    @GetMapping("/active")
    public ResponseEntity<ActiveLearningPathResponse> getActiveLearningPath(@PathVariable UUID userId) {
        log.info("[UserLearningPathController] Fetching active learning path for userId={}", userId);
        ActiveLearningPathResponse response = persistenceService.getActivePath(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all learning paths (active and archived) for the user, newest first.
     *
     * GET /api/users/{userId}/learning-paths
     */
    @GetMapping
    public ResponseEntity<List<LearningPathSummaryResponse>> getLearningPathHistory(@PathVariable UUID userId) {
        log.info("[UserLearningPathController] Fetching learning path history for userId={}", userId);
        List<LearningPathSummaryResponse> history = persistenceService.getPathHistory(userId);
        return ResponseEntity.ok(history);
    }
}
