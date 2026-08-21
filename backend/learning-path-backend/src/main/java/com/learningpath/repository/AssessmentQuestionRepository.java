package com.learningpath.repository;

import com.learningpath.entity.AssessmentQuestion;
import com.learningpath.entity.enums.CourseDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, UUID> {

    List<AssessmentQuestion> findAllByAssessmentId(UUID assessmentId);

    List<AssessmentQuestion> findAllByAssessmentIdAndDifficulty(UUID assessmentId, CourseDifficulty difficulty);
}
