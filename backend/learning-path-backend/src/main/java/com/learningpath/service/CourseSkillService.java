package com.learningpath.service;

import com.learningpath.dto.CourseResponse;
import com.learningpath.dto.CourseSkillRequest;
import com.learningpath.dto.CourseSkillResponse;
import com.learningpath.dto.CourseSkillUpdateRequest;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseSkillService {

    private final CourseSkillRepository courseSkillRepository;
    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;

    public CourseSkillResponse addCourseSkill(UUID courseId, CourseSkillRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.skillId()));

        if (courseSkillRepository.existsByCourseIdAndSkillId(courseId, request.skillId())) {
            throw new DuplicateResourceException("Course already has skill '" + skill.getName() + "' mapped");
        }

        ProficiencyLevel targetProficiency = request.targetProficiency() != null ? request.targetProficiency() : ProficiencyLevel.INTERMEDIATE;
        boolean isPrimarySkill = request.isPrimarySkill() != null ? request.isPrimarySkill() : false;

        CourseSkill courseSkill = CourseSkill.builder()
                .course(course)
                .skill(skill)
                .coverageLevel(request.coverageLevel())
                .importance(request.importance())
                .targetProficiency(targetProficiency)
                .isPrimarySkill(isPrimarySkill)
                .build();

        CourseSkill savedCourseSkill = courseSkillRepository.save(courseSkill);
        return mapToCourseSkillResponse(savedCourseSkill);
    }

    @Transactional(readOnly = true)
    public List<CourseSkillResponse> getCourseSkills(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return courseSkillRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToCourseSkillResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesBySkillId(UUID skillId) {
        if (!skillRepository.existsById(skillId)) {
            throw new ResourceNotFoundException("Skill not found with id: " + skillId);
        }

        return courseSkillRepository.findBySkillId(skillId)
                .stream()
                .map(CourseSkill::getCourse)
                .distinct()
                .map(this::mapToCourseResponse)
                .toList();
    }

    public CourseSkillResponse updateCourseSkill(UUID courseId, UUID skillId, CourseSkillUpdateRequest request) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        CourseSkill courseSkill = courseSkillRepository.findByCourseIdAndSkillId(courseId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not mapped to course " + courseId));

        if (request.coverageLevel() != null) {
            courseSkill.setCoverageLevel(request.coverageLevel());
        }
        if (request.importance() != null) {
            courseSkill.setImportance(request.importance());
        }
        if (request.targetProficiency() != null) {
            courseSkill.setTargetProficiency(request.targetProficiency());
        }
        if (request.isPrimarySkill() != null) {
            courseSkill.setPrimarySkill(request.isPrimarySkill());
        }

        CourseSkill updatedCourseSkill = courseSkillRepository.save(courseSkill);
        return mapToCourseSkillResponse(updatedCourseSkill);
    }

    public void removeCourseSkill(UUID courseId, UUID skillId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        CourseSkill courseSkill = courseSkillRepository.findByCourseIdAndSkillId(courseId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not mapped to course " + courseId));

        courseSkillRepository.delete(courseSkill);
    }

    private CourseSkillResponse mapToCourseSkillResponse(CourseSkill courseSkill) {
        return new CourseSkillResponse(
                courseSkill.getId(),
                courseSkill.getCourse().getId(),
                courseSkill.getCourse().getTitle(),
                courseSkill.getSkill().getId(),
                courseSkill.getSkill().getName(),
                courseSkill.getSkill().getCategory(),
                courseSkill.getCoverageLevel(),
                courseSkill.getImportance(),
                courseSkill.getTargetProficiency(),
                courseSkill.isPrimarySkill(),
                courseSkill.getCreatedAt(),
                courseSkill.getUpdatedAt()
        );
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getProvider(),
                course.getUrl(),
                course.getDifficulty(),
                course.getDurationHours(),
                course.getCourseType(),
                course.getLanguage(),
                course.getRating(),
                course.getPrice(),
                course.isFree(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
