package com.learningpath.recommendation.service;

import com.learningpath.entity.Course;
import com.learningpath.entity.RecommendationInteraction;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.RecommendationInteractionType;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.dto.RecordRecommendationInteractionRequest;
import com.learningpath.recommendation.dto.RecommendationInteractionResponse;
import com.learningpath.recommendation.dto.UserInteractionStatsResponse;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.RecommendationInteractionRepository;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationInteractionService {

    private final RecommendationInteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public RecommendationInteractionResponse recordInteraction(RecordRecommendationInteractionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.courseId()));

        RecommendationInteraction interaction = RecommendationInteraction.builder()
                .user(user)
                .course(course)
                .interactionType(request.interactionType())
                .recommendationRank(request.recommendationRank())
                .ruleBasedScore(request.ruleBasedScore())
                .mlScore(request.mlScore())
                .finalScore(request.finalScore())
                .build();

        RecommendationInteraction saved = interactionRepository.save(interaction);
        log.info("Recorded interaction [{}] for userId={} courseId={}", request.interactionType(), user.getId(), course.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RecommendationInteractionResponse> getUserInteractions(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return interactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecommendationInteractionResponse> getCourseInteractions(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return interactionRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserInteractionStatsResponse getUserInteractionStats(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        long total = interactionRepository.countByUserId(userId);
        long viewed = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.VIEWED);
        long clicked = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.CLICKED);
        long started = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.STARTED);
        long completed = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.COMPLETED);
        long liked = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.LIKED);
        long skipped = interactionRepository.countByUserIdAndInteractionType(userId, RecommendationInteractionType.SKIPPED);

        return new UserInteractionStatsResponse(total, viewed, clicked, started, completed, liked, skipped);
    }

    private RecommendationInteractionResponse mapToResponse(RecommendationInteraction entity) {
        return new RecommendationInteractionResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getCourse().getId(),
                entity.getInteractionType(),
                entity.getRecommendationRank(),
                entity.getRuleBasedScore(),
                entity.getMlScore(),
                entity.getFinalScore(),
                entity.getCreatedAt()
        );
    }
}
