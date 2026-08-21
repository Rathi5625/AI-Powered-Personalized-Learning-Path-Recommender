package com.learningpath.repository;

import com.learningpath.entity.LearningActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningActivityRepository extends JpaRepository<LearningActivity, UUID> {

    List<LearningActivity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
