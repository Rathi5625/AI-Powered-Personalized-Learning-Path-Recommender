package com.learningpath.repository;

import com.learningpath.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {

    List<UserProject> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<UserProject> findByUserIdAndProjectId(UUID userId, UUID projectId);

    long countByUserIdAndStatus(UUID userId, com.learningpath.entity.enums.ProjectStatus status);
}
