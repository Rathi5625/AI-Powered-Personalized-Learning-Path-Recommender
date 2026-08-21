package com.learningpath.repository;

import com.learningpath.entity.LearningPathVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningPathVersionRepository extends JpaRepository<LearningPathVersion, UUID> {
    List<LearningPathVersion> findByLearningPathIdOrderByVersionNumberDesc(UUID learningPathId);
    List<LearningPathVersion> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
