package com.learningpath.repository;

import com.learningpath.entity.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, UUID> {

    List<LearningPathItem> findByLearningPathIdOrderByItemOrderAsc(UUID learningPathId);

    List<LearningPathItem> findByLearningPathIdOrderByPhaseNumberAscItemOrderAsc(UUID learningPathId);

    void deleteByLearningPathId(UUID learningPathId);
}
