package com.learningpath.repository;

import com.learningpath.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);
    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);
    List<UserSkill> findByUserId(UUID userId);
    void deleteByUserIdAndSkillId(UUID userId, UUID skillId);
}
