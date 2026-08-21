package com.learningpath.repository;

import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.MasteryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerKnowledgeStateRepository extends JpaRepository<LearnerKnowledgeState, UUID> {

    List<LearnerKnowledgeState> findByUser(User user);

    List<LearnerKnowledgeState> findByUserId(UUID userId);

    Optional<LearnerKnowledgeState> findByUserIdAndConceptNameIgnoreCase(UUID userId, String conceptName);

    Optional<LearnerKnowledgeState> findByUserIdAndSkillId(UUID userId, UUID skillId);

    List<LearnerKnowledgeState> findByUserIdAndMasteryLevel(UUID userId, MasteryLevel masteryLevel);

    List<LearnerKnowledgeState> findByUserIdAndRevisionRequiredTrue(UUID userId);

    @Query("SELECT k FROM LearnerKnowledgeState k WHERE k.user.id = :userId ORDER BY k.knowledgeProbability ASC")
    List<LearnerKnowledgeState> findWeakestConceptsByUserId(@Param("userId") UUID userId);

    @Query("SELECT k FROM LearnerKnowledgeState k WHERE k.user.id = :userId ORDER BY k.knowledgeProbability DESC")
    List<LearnerKnowledgeState> findStrongestConceptsByUserId(@Param("userId") UUID userId);
}
