package com.learningpath.repository;

import com.learningpath.entity.AdaptiveAssessmentResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdaptiveAssessmentResponseRepository extends JpaRepository<AdaptiveAssessmentResponse, UUID> {

    @EntityGraph(attributePaths = {"session", "question"})
    List<AdaptiveAssessmentResponse> findBySessionIdOrderByAttemptNumberAsc(UUID sessionId);

    @EntityGraph(attributePaths = {"session", "question"})
    List<AdaptiveAssessmentResponse> findBySession_UserIdOrderByAnsweredAtDesc(UUID userId);
}
