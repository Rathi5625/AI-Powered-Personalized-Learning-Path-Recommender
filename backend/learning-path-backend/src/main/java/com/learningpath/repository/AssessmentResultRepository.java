package com.learningpath.repository;

import com.learningpath.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {

    List<AssessmentResult> findAllByUserIdOrderByCompletedAtDesc(UUID userId);

    Optional<AssessmentResult> findTopByUserIdAndAssessmentIdOrderByCompletedAtDesc(UUID userId, UUID assessmentId);

    long countByUserId(UUID userId);
}
