package com.learningpath.repository;

import com.learningpath.entity.CourseSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseSkillRepository extends JpaRepository<CourseSkill, UUID> {
    boolean existsByCourseIdAndSkillId(UUID courseId, UUID skillId);

    @EntityGraph(attributePaths = {"course", "skill"})
    Optional<CourseSkill> findByCourseIdAndSkillId(UUID courseId, UUID skillId);

    @EntityGraph(attributePaths = {"course", "skill"})
    List<CourseSkill> findByCourseId(UUID courseId);

    @EntityGraph(attributePaths = {"course", "skill"})
    List<CourseSkill> findBySkillId(UUID skillId);

    void deleteByCourseIdAndSkillId(UUID courseId, UUID skillId);
}
