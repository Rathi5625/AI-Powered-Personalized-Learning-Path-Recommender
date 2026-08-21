package com.learningpath.repository;

import com.learningpath.entity.AdaptiveAssessmentSession;
import com.learningpath.entity.enums.AdaptiveSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdaptiveAssessmentSessionRepository extends JpaRepository<AdaptiveAssessmentSession, UUID> {

    @EntityGraph(attributePaths = {"user", "assessment"})
    Optional<AdaptiveAssessmentSession> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"user", "assessment"})
    List<AdaptiveAssessmentSession> findByUserIdAndAssessmentIdAndStatus(UUID userId, UUID assessmentId, AdaptiveSessionStatus status);

    @EntityGraph(attributePaths = {"user", "assessment"})
    List<AdaptiveAssessmentSession> findByUserIdOrderByStartedAtDesc(UUID userId);
}
