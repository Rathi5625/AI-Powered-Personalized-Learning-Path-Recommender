package com.learningpath.skilldependency.controller;

import com.learningpath.skilldependency.dto.*;
import com.learningpath.skilldependency.service.SkillDependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills/dependencies")
@RequiredArgsConstructor
public class SkillDependencyController {

    private final SkillDependencyService dependencyService;

    @GetMapping("/{skillName}")
    public ResponseEntity<PrerequisitesResponse> getPrerequisites(@PathVariable String skillName) {
        PrerequisitesResponse response = dependencyService.getPrerequisites(skillName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{skillName}/dependents")
    public ResponseEntity<DependentsResponse> getDependents(@PathVariable String skillName) {
        DependentsResponse response = dependencyService.getDependents(skillName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/learning-order")
    public ResponseEntity<LearningOrderResponse> getLearningOrder(@Valid @RequestBody LearningOrderRequest request) {
        LearningOrderResponse response = dependencyService.getLearningOrder(request.skills());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/missing")
    public ResponseEntity<MissingPrerequisitesResponse> getMissingPrerequisites(
            @RequestBody MissingPrerequisitesRequest request
    ) {
        List<String> current = (request != null) ? request.currentSkills() : null;
        List<String> target = (request != null) ? request.targetSkills() : null;

        MissingPrerequisitesResponse response = dependencyService.getMissingPrerequisites(current, target);
        return ResponseEntity.ok(response);
    }
}
