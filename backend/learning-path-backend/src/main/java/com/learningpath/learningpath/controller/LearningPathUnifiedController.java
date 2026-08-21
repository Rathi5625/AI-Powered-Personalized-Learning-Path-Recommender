package com.learningpath.learningpath.controller;

import com.learningpath.entity.LearningPath;
import com.learningpath.entity.LearningPathVersion;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.learningpath.dto.*;
import com.learningpath.learningpath.service.CareerSkillGapService;
import com.learningpath.learningpath.service.LearningPathEngineService;
import com.learningpath.learningpath.service.LearningPathRecalculationService;
import com.learningpath.learningpath.service.WeeklyLearningPlanService;
import com.learningpath.repository.LearningPathRepository;
import com.learningpath.repository.LearningPathVersionRepository;
import com.learningpath.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning-path")
@RequiredArgsConstructor
@Slf4j
public class LearningPathUnifiedController {

    private final LearningPathEngineService engineService;
    private final LearningPathRecalculationService recalculationService;
    private final WeeklyLearningPlanService weeklyPlanService;
    private final CareerSkillGapService skillGapService;
    private final LearningPathRepository learningPathRepository;
    private final LearningPathVersionRepository versionRepository;

    @GetMapping
    public ResponseEntity<LearningPathFullResponse> getCurrentLearningPath(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        UUID userId = principal.getId();

        LearningPath active = learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE).orElse(null);
        UUID careerId = (active != null && active.getTargetCareer() != null) ? active.getTargetCareer().getId() : null;

        LearningPathFullResponse response = engineService.generatePath(userId, careerId, "Loaded active curriculum");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<LearningPathFullResponse> generateLearningPath(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID careerId
    ) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        LearningPathFullResponse response = engineService.generatePath(principal.getId(), careerId, "User requested path generation");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<LearningPathFullResponse> recalculateLearningPath(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "Manual recalculation trigger") String reason
    ) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        LearningPathFullResponse response = recalculationService.triggerRecalculation(principal.getId(), reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly-plan")
    public ResponseEntity<WeeklyLearningPlanDto> getWeeklyPlan(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        return ResponseEntity.ok(weeklyPlanService.getWeeklyPlan(principal.getId()));
    }

    @GetMapping("/skill-gaps")
    public ResponseEntity<List<SkillGapDetailDto>> getSkillGaps(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID careerId
    ) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        return ResponseEntity.ok(skillGapService.analyzeGaps(principal.getId(), careerId));
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<LearningPathNodeDto>> getNodes(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        LearningPathFullResponse path = engineService.generatePath(principal.getId(), null, "Node query");
        return ResponseEntity.ok(path.getNodes());
    }

    @GetMapping("/changes")
    public ResponseEntity<List<PathChangeHistoryDto>> getChanges(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        List<LearningPathVersion> versions = versionRepository.findByUserIdOrderByCreatedAtDesc(principal.getId());

        List<PathChangeHistoryDto> dtos = versions.stream()
                .map(v -> PathChangeHistoryDto.builder()
                        .version(v.getVersionNumber())
                        .timestamp(v.getCreatedAt())
                        .reason(v.getChangeReason())
                        .explanation(v.getExplanation())
                        .overallProgress(v.getOverallProgress())
                        .build())
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/milestones")
    public ResponseEntity<List<LearningPathMilestoneDto>> getMilestones(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new AccessDeniedException("User not authenticated");
        LearningPathFullResponse path = engineService.generatePath(principal.getId(), null, "Milestone query");
        return ResponseEntity.ok(path.getMilestones());
    }
}
