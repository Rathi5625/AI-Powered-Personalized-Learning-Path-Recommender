package com.learningpath.repository;

import com.learningpath.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, UUID id);
    Optional<Course> findByTitle(String title);
    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Course> findByTitleContainingIgnoreCase(String title);
}
