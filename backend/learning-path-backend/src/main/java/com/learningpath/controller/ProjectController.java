package com.learningpath.controller;

import com.learningpath.dto.ProjectDto;
import com.learningpath.dto.UpdateUserProjectRequest;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getProjects(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(projectService.getProjectsForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProjectProgress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) UpdateUserProjectRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(projectService.startOrUpdateProject(principal.getId(), id, request));
    }
}
