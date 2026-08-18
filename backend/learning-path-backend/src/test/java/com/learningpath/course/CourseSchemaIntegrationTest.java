package com.learningpath.course;

import com.learningpath.dto.CourseRequest;
import com.learningpath.dto.CourseResponse;
import com.learningpath.entity.Course;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.repository.CourseRepository;
import com.learningpath.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:course_schema_testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CourseSchemaIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Test
    @DisplayName("1. Persist Course with dataset courseCode and YouTube metadata")
    void testCoursePersistence_WithDatasetFields() {
        Course course = Course.builder()
                .courseCode("C001")
                .title("MDN: Structuring content with HTML")
                .provider("MDN")
                .url("https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Structuring_content")
                .difficulty(CourseDifficulty.BEGINNER)
                .durationHours(3.0)
                .courseType(CourseType.DOCUMENTATION)
                .isFree(true)
                .youtubeTitle("HTML Structure Overview")
                .youtubeUrl("https://youtube.com/watch?v=sample-html")
                .youtubeNotes("Official companion overview")
                .build();

        Course saved = courseRepository.save(course);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourseCode()).isEqualTo("C001");
        assertThat(saved.getYoutubeTitle()).isEqualTo("HTML Structure Overview");
        assertThat(saved.getYoutubeUrl()).isEqualTo("https://youtube.com/watch?v=sample-html");
        assertThat(saved.getYoutubeNotes()).isEqualTo("Official companion overview");

        Optional<Course> byCode = courseRepository.findByCourseCode("C001");
        assertThat(byCode).isPresent();
        assertThat(byCode.get().getTitle()).isEqualTo("MDN: Structuring content with HTML");
        assertThat(courseRepository.existsByCourseCode("C001")).isTrue();
    }

    @Test
    @DisplayName("2. Persist Course with null dataset fields for backward compatibility")
    void testCoursePersistence_WithNullableFields() {
        Course course = Course.builder()
                .title("Standard Legacy Course Without Code")
                .provider("Legacy Provider")
                .url("https://example.com/legacy")
                .difficulty(CourseDifficulty.INTERMEDIATE)
                .durationHours(10.0)
                .courseType(CourseType.VIDEO_COURSE)
                .isFree(false)
                .build();

        Course saved = courseRepository.save(course);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourseCode()).isNull();
        assertThat(saved.getYoutubeTitle()).isNull();
        assertThat(saved.getYoutubeUrl()).isNull();
        assertThat(saved.getYoutubeNotes()).isNull();

        assertThat(courseRepository.existsByCourseCode("NON_EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("3. CourseService create and update with new dataset DTO fields")
    void testCourseService_CreateAndUpdate_WithNewFields() {
        CourseRequest request = new CourseRequest(
                "C042",
                "Advanced CSS Layout and Architecture",
                "Deep dive into CSS Grid, Flexbox, Container Queries, and Subgrid",
                "web.dev",
                "https://web.dev/learn/css",
                CourseDifficulty.HIGH,
                15.0,
                CourseType.DOCUMENTATION,
                "English",
                new BigDecimal("4.90"),
                BigDecimal.ZERO,
                true,
                "CSS Layout Deep Dive",
                "https://youtube.com/watch?v=sample-css",
                "Recommended for senior frontend engineers"
        );

        CourseResponse created = courseService.createCourse(request);
        assertThat(created.id()).isNotNull();
        assertThat(created.courseCode()).isEqualTo("C042");
        assertThat(created.difficulty()).isEqualTo(CourseDifficulty.HIGH);
        assertThat(created.youtubeTitle()).isEqualTo("CSS Layout Deep Dive");

        // Verify lookup via service
        CourseResponse fetched = courseService.getCourseById(created.id());
        assertThat(fetched.courseCode()).isEqualTo("C042");
        assertThat(fetched.provider()).isEqualTo("web.dev");

        // Update course
        CourseRequest updateRequest = new CourseRequest(
                "C042",
                "Advanced CSS Layout & Architecture (Updated)",
                "Updated description",
                "web.dev",
                "https://web.dev/learn/css",
                CourseDifficulty.HIGH,
                16.0,
                CourseType.DOCUMENTATION,
                "English",
                new BigDecimal("4.95"),
                BigDecimal.ZERO,
                true,
                "CSS Layout Deep Dive 2026",
                "https://youtube.com/watch?v=sample-css-2026",
                "Updated notes"
        );

        CourseResponse updated = courseService.updateCourse(created.id(), updateRequest);
        assertThat(updated.title()).isEqualTo("Advanced CSS Layout & Architecture (Updated)");
        assertThat(updated.durationHours()).isEqualTo(16.0);
        assertThat(updated.youtubeTitle()).isEqualTo("CSS Layout Deep Dive 2026");
    }

    @Test
    @DisplayName("4. Verify CourseDifficulty enum parsing from dataset levels")
    void testCourseDifficulty_FromDatasetLevel() {
        assertThat(CourseDifficulty.fromDatasetLevel("Beginner")).isEqualTo(CourseDifficulty.BEGINNER);
        assertThat(CourseDifficulty.fromDatasetLevel("Easy")).isEqualTo(CourseDifficulty.EASY);
        assertThat(CourseDifficulty.fromDatasetLevel("Medium")).isEqualTo(CourseDifficulty.MEDIUM);
        assertThat(CourseDifficulty.fromDatasetLevel("High")).isEqualTo(CourseDifficulty.HIGH);
        assertThat(CourseDifficulty.fromDatasetLevel("Hard")).isEqualTo(CourseDifficulty.HIGH);
        assertThat(CourseDifficulty.fromDatasetLevel("Advanced")).isEqualTo(CourseDifficulty.ADVANCED);
        assertThat(CourseDifficulty.fromDatasetLevel("All_Levels")).isEqualTo(CourseDifficulty.ALL_LEVELS);
        assertThat(CourseDifficulty.fromDatasetLevel(null)).isEqualTo(CourseDifficulty.BEGINNER);
    }

    @Test
    @DisplayName("5. Verify CourseType enum parsing from provider platforms")
    void testCourseType_FromPlatform() {
        assertThat(CourseType.fromPlatform("MDN")).isEqualTo(CourseType.DOCUMENTATION);
        assertThat(CourseType.fromPlatform("web.dev")).isEqualTo(CourseType.TEXT_TUTORIAL);
        assertThat(CourseType.fromPlatform("freeCodeCamp")).isEqualTo(CourseType.INTERACTIVE_COURSE);
        assertThat(CourseType.fromPlatform("YouTube")).isEqualTo(CourseType.VIDEO_COURSE);
        assertThat(CourseType.fromPlatform("Coursera")).isEqualTo(CourseType.VIDEO_COURSE);
        assertThat(CourseType.fromPlatform("Microsoft Docs")).isEqualTo(CourseType.DOCUMENTATION);
    }

    @Test
    @DisplayName("6. Existing seeded courses remain accessible and intact")
    void testExistingSeededCourses_Intact() {
        Page<CourseResponse> allCourses = courseService.getAllCourses(PageRequest.of(0, 50));
        assertThat(allCourses.getTotalElements()).isGreaterThanOrEqualTo(20);
        assertThat(courseRepository.existsByTitle("Java Programming Fundamentals")).isTrue();
    }
}
