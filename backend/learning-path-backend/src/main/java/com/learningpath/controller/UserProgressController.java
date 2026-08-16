package com.learningpath.controller;

import com.learningpath.dto.LearningProgressRequest;
import com.learningpath.dto.LearningProgressResponse;
import com.learningpath.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing learner course progress.
 *
 * Base path: /api/users/{userId}/learning-progress
 */
@RestController
@RequestMapping("/api/users/{userId}/learning-progress")
@RequiredArgsConstructor
@Slf4j
public class UserProgressController {

    private final UserProgressService userProgressService;

    /**
     * Create or update progress for a specific course.
     *
     * PUT /api/users/{userId}/learning-progress/{courseId}
     * Idempotent: calling multiple times with the same (userId, courseId) updates the existing record.
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<LearningProgressResponse> upsertProgress(
            @PathVariable UUID userId,
            @PathVariable UUID courseId,
            @Valid @RequestBody LearningProgressRequest request
    ) {
        log.info("[UserProgressController] Upserting progress for userId={}, courseId={}, status={}",
                userId, courseId, request.status());
        LearningProgressResponse response = userProgressService.upsertProgress(userId, courseId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieve all progress records for a learner.
     *
     * GET /api/users/{userId}/learning-progress
     */
    @GetMapping
    public ResponseEntity<List<LearningProgressResponse>> getUserProgress(@PathVariable UUID userId) {
        log.info("[UserProgressController] Fetching all progress for userId={}", userId);
        List<LearningProgressResponse> progress = userProgressService.getUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Retrieve progress for a specific course.
     *
     * GET /api/users/{userId}/learning-progress/{courseId}
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<LearningProgressResponse> getCourseProgress(
            @PathVariable UUID userId,
            @PathVariable UUID courseId
    ) {
        log.info("[UserProgressController] Fetching progress for userId={}, courseId={}", userId, courseId);
        LearningProgressResponse response = userProgressService.getCourseProgress(userId, courseId);
        return ResponseEntity.ok(response);
    }
}
