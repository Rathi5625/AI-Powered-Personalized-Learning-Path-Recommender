package com.learningpath.controller;

import com.learningpath.dto.AssessmentDto;
import com.learningpath.dto.AssessmentResultDto;
import com.learningpath.dto.AssessmentSubmissionRequest;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<List<AssessmentDto>> getAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> getAssessmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssessmentResultDto> submitAssessment(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssessmentSubmissionRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(assessmentService.submitAssessment(principal.getId(), id, request));
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<AssessmentResultDto>> getMyAssessmentResults(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(assessmentService.getUserAssessmentResults(principal.getId()));
    }
}
