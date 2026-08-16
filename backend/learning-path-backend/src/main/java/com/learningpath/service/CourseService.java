package com.learningpath.service;

import com.learningpath.dto.CourseRequest;
import com.learningpath.dto.CourseResponse;
import com.learningpath.entity.Course;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByTitle(request.title())) {
            throw new DuplicateResourceException("Course with title '" + request.title() + "' already exists");
        }

        boolean isFree = request.isFree() != null ? request.isFree() : (request.price() != null && request.price().compareTo(BigDecimal.ZERO) == 0);
        String language = request.language() != null && !request.language().isBlank() ? request.language() : "English";

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .provider(request.provider())
                .url(request.url())
                .difficulty(request.difficulty())
                .durationHours(request.durationHours())
                .courseType(request.courseType())
                .language(language)
                .rating(request.rating())
                .price(request.price())
                .isFree(isFree)
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToCourseResponse(savedCourse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(this::mapToCourseResponse);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapToCourseResponse(course);
    }

    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (courseRepository.existsByTitleAndIdNot(request.title(), id)) {
            throw new DuplicateResourceException("Course with title '" + request.title() + "' already exists");
        }

        boolean isFree = request.isFree() != null ? request.isFree() : (request.price() != null && request.price().compareTo(BigDecimal.ZERO) == 0);
        String language = request.language() != null && !request.language().isBlank() ? request.language() : "English";

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setProvider(request.provider());
        course.setUrl(request.url());
        course.setDifficulty(request.difficulty());
        course.setDurationHours(request.durationHours());
        course.setCourseType(request.courseType());
        course.setLanguage(language);
        course.setRating(request.rating());
        course.setPrice(request.price());
        course.setFree(isFree);

        Course updatedCourse = courseRepository.save(course);
        return mapToCourseResponse(updatedCourse);
    }

    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> searchCoursesByTitle(String title) {
        return courseRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToCourseResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> filterCourses(
            CourseDifficulty difficulty,
            CourseType courseType,
            String provider,
            Boolean isFree,
            String language,
            Pageable pageable
    ) {
        Specification<Course> spec = CourseSpecification.filterCourses(difficulty, courseType, provider, isFree, language);
        return courseRepository.findAll(spec, pageable)
                .map(this::mapToCourseResponse);
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
