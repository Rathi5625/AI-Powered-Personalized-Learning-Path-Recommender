package com.learningpath.repository;

import com.learningpath.entity.LearningPath;
import com.learningpath.entity.enums.LearningPathStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {

    Optional<LearningPath> findByUserIdAndStatus(UUID userId, LearningPathStatus status);

    List<LearningPath> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LearningPath> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, LearningPathStatus status);

    boolean existsByUserIdAndStatus(UUID userId, LearningPathStatus status);

    @Modifying
    @Query("UPDATE LearningPath lp SET lp.status = :newStatus WHERE lp.user.id = :userId AND lp.status = :oldStatus")
    int updateStatusByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("oldStatus") LearningPathStatus oldStatus,
            @Param("newStatus") LearningPathStatus newStatus
    );
}
