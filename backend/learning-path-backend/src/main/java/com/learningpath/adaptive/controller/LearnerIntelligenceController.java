package com.learningpath.adaptive.controller;

import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.dto.DailyLearningPlanDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.AdaptiveAssessmentService;
import com.learningpath.adaptive.service.AdaptiveDifficultyService;
import com.learningpath.adaptive.service.LearnerBehaviorService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.adaptive.service.PersonalizedLearningPlanService;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class LearnerIntelligenceController {

    private final LearnerMasteryService masteryService;
    private final LearnerBehaviorService behaviorService;
    private final AdaptiveDifficultyService difficultyService;
    private final AdaptiveAssessmentService assessmentService;
    private final PersonalizedLearningPlanService planService;
    private final UserRepository userRepository;

    @GetMapping("/learner/mastery")
    public ResponseEntity<LearnerMasteryDto.Summary> getMastery(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(masteryService.getMasterySummary(user.getId()));
    }

    @GetMapping("/learner/weak-skills")
    public ResponseEntity<List<LearnerMasteryDto.ConceptItem>> getWeakSkills(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(masteryService.getWeakSkills(user.getId()));
    }

    @GetMapping("/learner/revision")
    public ResponseEntity<List<LearnerMasteryDto.ConceptItem>> getRevisionRequired(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(masteryService.getRevisionRequired(user.getId()));
    }

    @GetMapping("/learner/behavior")
    public ResponseEntity<LearnerBehaviorProfile> getBehavior(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(behaviorService.getBehaviorProfile(user.getId()));
    }

    @GetMapping("/learner/difficulty")
    public ResponseEntity<Map<String, Object>> getDifficulty(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false, defaultValue = "General") String concept
    ) {
        User user = resolveUser(principal);
        CourseDifficulty diff = difficultyService.determineDifficulty(user.getId(), concept, CourseDifficulty.BEGINNER);
        return ResponseEntity.ok(Map.of("concept", concept, "recommendedDifficulty", diff));
    }

    @GetMapping("/learning-plan")
    public ResponseEntity<DailyLearningPlanDto> getLearningPlan(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(planService.generateDailyPlan(user));
    }

    @PostMapping("/learning-plan/generate")
    public ResponseEntity<DailyLearningPlanDto> generateLearningPlan(@AuthenticationPrincipal UserPrincipal principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(planService.generateDailyPlan(user));
    }

    @GetMapping("/assessments/{id}/adaptive-question")
    public ResponseEntity<AdaptiveAssessmentDto.QuestionResponse> getAdaptiveQuestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.getAdaptiveQuestion(id, user));
    }

    @PostMapping("/assessments/{id}/adaptive-answer")
    public ResponseEntity<AdaptiveAssessmentDto.AnswerResult> submitAdaptiveAnswer(
            @PathVariable UUID id,
            @RequestBody AdaptiveAssessmentDto.AnswerRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.processAdaptiveAnswer(id, request, user));
    }

    // ==========================================
    // Phase 6 — Advanced CAT Session Endpoints
    // ==========================================
    @PostMapping("/assessments/{id}/adaptive/start")
    public ResponseEntity<AdaptiveAssessmentDto.SessionStartResponse> startAdaptiveSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.startSession(id, user));
    }

    @GetMapping("/assessments/adaptive/{sessionId}/next-question")
    public ResponseEntity<AdaptiveAssessmentDto.NextQuestionResponse> getAdaptiveNextQuestion(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.getNextQuestion(sessionId, user));
    }

    @PostMapping("/assessments/adaptive/{sessionId}/answer")
    public ResponseEntity<AdaptiveAssessmentDto.AnswerSubmissionResult> submitAdaptiveSessionAnswer(
            @PathVariable UUID sessionId,
            @RequestBody AdaptiveAssessmentDto.AnswerSubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.submitAnswer(sessionId, request, user));
    }

    @GetMapping("/assessments/adaptive/{sessionId}/result")
    public ResponseEntity<AdaptiveAssessmentDto.SessionResultResponse> getAdaptiveSessionResult(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.getSessionResult(sessionId, user));
    }

    @GetMapping("/assessments/adaptive/{sessionId}/analytics")
    public ResponseEntity<AdaptiveAssessmentDto.SessionAnalyticsResponse> getAdaptiveSessionAnalytics(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(assessmentService.getSessionAnalytics(sessionId, user));
    }

    private User resolveUser(UserPrincipal principal) {
        if (principal != null) {
            return userRepository.findById(principal.getId())
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElseThrow(() -> new IllegalStateException("No user in database")));
        }
        return userRepository.findAll().stream().findFirst().orElseThrow(() -> new IllegalStateException("No user in database"));
    }
}

