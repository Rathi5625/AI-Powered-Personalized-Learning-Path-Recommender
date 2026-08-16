package com.learningpath.repository;

import com.learningpath.entity.RecommendationInteraction;
import com.learningpath.entity.enums.RecommendationInteractionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationInteractionRepository extends JpaRepository<RecommendationInteraction, UUID> {

    @EntityGraph(attributePaths = {"user", "course"})
    List<RecommendationInteraction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"user", "course"})
    List<RecommendationInteraction> findByCourseIdOrderByCreatedAtDesc(UUID courseId);

    @EntityGraph(attributePaths = {"user", "course"})
    List<RecommendationInteraction> findByUserIdAndInteractionType(UUID userId, RecommendationInteractionType interactionType);

    long countByUserIdAndInteractionType(UUID userId, RecommendationInteractionType interactionType);

    long countByUserId(UUID userId);
}
