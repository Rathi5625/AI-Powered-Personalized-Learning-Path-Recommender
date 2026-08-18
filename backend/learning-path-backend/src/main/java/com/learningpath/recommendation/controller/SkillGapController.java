package com.learningpath.recommendation.controller;

import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/skill-gaps")
@RequiredArgsConstructor
public class SkillGapController {

    private final SkillGapService skillGapService;

    @GetMapping
    public ResponseEntity<SkillGapAnalysisResponse> getSkillGaps(
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID careerId
    ) {
        SkillGapAnalysisResponse response = skillGapService.analyzeSkillGap(userId, careerId);
        return ResponseEntity.ok(response);
    }
}
