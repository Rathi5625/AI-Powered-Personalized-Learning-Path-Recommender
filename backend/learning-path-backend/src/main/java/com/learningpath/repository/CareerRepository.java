package com.learningpath.repository;

import com.learningpath.entity.Career;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareerRepository extends JpaRepository<Career, UUID> {
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, UUID id);
    Optional<Career> findByTitle(String title);
    Page<Career> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Career> findByTitleContainingIgnoreCase(String title);
}
