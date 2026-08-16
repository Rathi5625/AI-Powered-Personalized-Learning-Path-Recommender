package com.learningpath.controller;

import com.learningpath.dto.CourseSkillRequest;
import com.learningpath.dto.CourseSkillResponse;
import com.learningpath.dto.CourseSkillUpdateRequest;
import com.learningpath.service.CourseSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses/{courseId}/skills")
@RequiredArgsConstructor
public class CourseSkillController {

    private final CourseSkillService courseSkillService;

    @PostMapping
    public ResponseEntity<CourseSkillResponse> addCourseSkill(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseSkillRequest request
    ) {
        CourseSkillResponse response = courseSkillService.addCourseSkill(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseSkillResponse>> getCourseSkills(@PathVariable UUID courseId) {
        List<CourseSkillResponse> skills = courseSkillService.getCourseSkills(courseId);
        return ResponseEntity.ok(skills);
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<CourseSkillResponse> updateCourseSkill(
            @PathVariable UUID courseId,
            @PathVariable UUID skillId,
            @Valid @RequestBody CourseSkillUpdateRequest request
    ) {
        CourseSkillResponse response = courseSkillService.updateCourseSkill(courseId, skillId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> removeCourseSkill(
            @PathVariable UUID courseId,
            @PathVariable UUID skillId
    ) {
        courseSkillService.removeCourseSkill(courseId, skillId);
        return ResponseEntity.noContent().build();
    }
}
