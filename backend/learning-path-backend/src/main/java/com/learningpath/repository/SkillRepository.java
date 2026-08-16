package com.learningpath.repository;

import com.learningpath.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    Optional<Skill> findByName(String name);
    Page<Skill> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Skill> findByNameContainingIgnoreCase(String name);
}
