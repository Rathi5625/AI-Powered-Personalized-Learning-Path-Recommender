package com.learningpath.repository;

import com.learningpath.entity.CareerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareerSkillRepository extends JpaRepository<CareerSkill, UUID> {
    boolean existsByCareerIdAndSkillId(UUID careerId, UUID skillId);
    Optional<CareerSkill> findByCareerIdAndSkillId(UUID careerId, UUID skillId);
    List<CareerSkill> findByCareerId(UUID careerId);
    void deleteByCareerIdAndSkillId(UUID careerId, UUID skillId);
}
