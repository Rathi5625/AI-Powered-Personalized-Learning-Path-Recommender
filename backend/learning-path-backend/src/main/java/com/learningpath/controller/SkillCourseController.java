package com.learningpath.controller;

import com.learningpath.dto.CourseResponse;
import com.learningpath.service.CourseSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills/{skillId}/courses")
@RequiredArgsConstructor
public class SkillCourseController {

    private final CourseSkillService courseSkillService;

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCoursesForSkill(@PathVariable UUID skillId) {
        List<CourseResponse> courses = courseSkillService.getCoursesBySkillId(skillId);
        return ResponseEntity.ok(courses);
    }
}
