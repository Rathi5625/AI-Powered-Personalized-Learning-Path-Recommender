package com.learningpath.adaptive.service;

import com.learningpath.adaptive.dto.DailyLearningPlanDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.ai.service.LearnerContextService;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.service.LearnerFeatureBuilderService;
import com.learningpath.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizedLearningPlanService {

    private final LearnerMasteryService masteryService;
    private final LearnerBehaviorService behaviorService;
    private final LearnerContextService contextService;
    private final LearnerFeatureBuilderService featureBuilderService;
    private final MlRecommendationClient mlRecommendationClient;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public DailyLearningPlanDto generateDailyPlan(User user) {
        LearnerMasteryDto.Summary mastery = masteryService.getMasterySummary(user.getId());
        LearnerBehaviorProfile behavior = behaviorService.getBehaviorProfile(user.getId());
        LearnerAiContext context = contextService.buildContext(user);

        // 1. Identify primary focus concept
        String focusTopic = "Binary Search";
        double focusProb = 0.45;

        if (!mastery.getWeakSkills().isEmpty()) {
            focusTopic = mastery.getWeakSkills().get(0);
            focusProb = 0.35;
        } else if (!mastery.getDevelopingSkills().isEmpty()) {
            focusTopic = mastery.getDevelopingSkills().get(0);
            focusProb = 0.55;
        }

        // 2. ML Ranked top candidate course
        List<Course> courses = courseRepository.findAll();
        Course topCourse = null;
        double bestScore = -1.0;

        for (Course c : courses) {
            MlPredictionRequest req = featureBuilderService.buildFeatureVector(context, c);
            Optional<MlPredictionResponse> mlRes = mlRecommendationClient.predict(req);
            double score = mlRes.map(MlPredictionResponse::recommendationScore).orElse(75.0);
            if (score > bestScore) {
                bestScore = score;
                topCourse = c;
            }
        }

        String topCourseTitle = topCourse != null ? topCourse.getTitle() : "Mastering Data Structures & Algorithms";
        String topCourseUrl = topCourse != null && topCourse.getUrl() != null ? topCourse.getUrl() : "/explore-courses";

        // 3. Assemble multi-stage plan items
        List<DailyLearningPlanDto.PlanItemDto> items = new ArrayList<>();

        // Item 1: Concept Mastery
        items.add(DailyLearningPlanDto.PlanItemDto.builder()
                .id("plan-1")
                .title("Master " + focusTopic + " Fundamentals")
                .type("LEARN")
                .durationMinutes(20)
                .difficulty(behavior.getPreferredDifficulty())
                .reason("Knowledge probability at " + Math.round(focusProb * 100) + "%; core building block for " + (context != null ? context.getTargetCareer() : "target career"))
                .actionUrl(topCourseUrl)
                .priority(1)
                .build());

        // Item 2: Practice Challenge
        items.add(DailyLearningPlanDto.PlanItemDto.builder()
                .id("plan-2")
                .title(focusTopic + " 3-Problem Practice Set")
                .type("PRACTICE")
                .durationMinutes(15)
                .difficulty(behavior.getPreferredDifficulty())
                .reason("Reinforce pattern recognition with immediate feedback")
                .actionUrl("/assessments")
                .priority(2)
                .build());

        // Item 3: Revision or ML Course Deep Dive
        if (!mastery.getRevisionRequiredSkills().isEmpty()) {
            String revSkill = mastery.getRevisionRequiredSkills().get(0);
            items.add(DailyLearningPlanDto.PlanItemDto.builder()
                    .id("plan-3")
                    .title("Review Prerequisite: " + revSkill)
                    .type("REVISION")
                    .durationMinutes(10)
                    .difficulty("BEGINNER")
                    .reason("Flagged for periodic revision to prevent memory decay")
                    .actionUrl("/learning-path")
                    .priority(3)
                    .build());
        } else {
            items.add(DailyLearningPlanDto.PlanItemDto.builder()
                    .id("plan-3")
                    .title("Course Deep Dive: " + topCourseTitle)
                    .type("LEARN")
                    .durationMinutes(15)
                    .difficulty(behavior.getPreferredDifficulty())
                    .reason("Top ML recommendation matching career priority (Match: " + Math.round(bestScore) + "%)")
                    .actionUrl(topCourseUrl)
                    .priority(3)
                    .build());
        }

        int totalMinutes = items.stream().mapToInt(DailyLearningPlanDto.PlanItemDto::getDurationMinutes).sum();

        return DailyLearningPlanDto.builder()
                .title("Today's Adaptive Learning Plan")
                .targetCareer(context != null ? context.getTargetCareer() : "Software Engineer")
                .estimatedTotalMinutes(totalMinutes)
                .focusTopic(focusTopic)
                .currentMasteryProbability(focusProb)
                .items(items)
                .generatedAt(Instant.now().toString())
                .build();
    }
}
