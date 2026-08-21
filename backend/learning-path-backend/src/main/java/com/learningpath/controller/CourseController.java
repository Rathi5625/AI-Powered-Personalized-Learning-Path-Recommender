package com.learningpath.controller;

import com.learningpath.dto.CourseEnrollmentResponse;
import com.learningpath.dto.CourseProgressUpdateDto;
import com.learningpath.dto.CourseRequest;
import com.learningpath.dto.CourseResponse;
import com.learningpath.dto.LearningProgressRequest;
import com.learningpath.dto.LearningProgressResponse;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.CourseService;
import com.learningpath.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserProgressService userProgressService;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CourseResponse> courses = courseService.getAllCourses(pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(List.of(
                "All Courses",
                "Computer Science",
                "Web Development",
                "Data Science",
                "Artificial Intelligence",
                "Cloud & DevOps",
                "Cybersecurity",
                "Mobile Development",
                "Algorithms & Data Structures"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable UUID id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String title) {
        List<CourseResponse> courses = courseService.searchCoursesByTitle(title);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<CourseResponse>> filterCourses(
            @RequestParam(required = false) CourseDifficulty difficulty,
            @RequestParam(required = false) CourseType courseType,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CourseResponse> courses = courseService.filterCourses(difficulty, courseType, provider, isFree, language, pageable);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<LearningProgressResponse> enrollInCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        LearningProgressRequest request = new LearningProgressRequest(ProgressStatus.IN_PROGRESS, BigDecimal.ZERO);
        LearningProgressResponse response = userProgressService.upsertProgress(principal.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<LearningProgressResponse> getCourseProgress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(userProgressService.getCourseProgress(principal.getId(), id));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<LearningProgressResponse> updateCourseProgress(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CourseProgressUpdateDto request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        LearningProgressRequest progressReq = new LearningProgressRequest(
                request.status(),
                request.progressPercentage() != null ? BigDecimal.valueOf(request.progressPercentage()) : BigDecimal.ZERO
        );
        return ResponseEntity.ok(userProgressService.upsertProgress(principal.getId(), id, progressReq));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<LearningProgressResponse>> getMyCourses(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(userProgressService.getUserProgress(principal.getId()));
    }
}
