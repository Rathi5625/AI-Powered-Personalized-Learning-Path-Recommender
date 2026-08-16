package com.learningpath;

import com.learningpath.dto.CourseResponse;
import com.learningpath.dto.CourseSkillResponse;
import com.learningpath.dto.SkillResponse;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.service.CourseService;
import com.learningpath.service.CourseSkillService;
import com.learningpath.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_course;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CourseIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private CourseSkillService courseSkillService;

    @Test
    void testSeedCoursesAndSkillLookups() {
        // 1. Verify Seed Courses Exist
        Page<CourseResponse> allCourses = courseService.getAllCourses(PageRequest.of(0, 50));
        assertTrue(allCourses.getTotalElements() >= 20, "Should have seeded at least 20 courses");

        // 2. Search for "Spring Boot" Courses
        List<CourseResponse> springCourses = courseService.searchCoursesByTitle("Spring Boot");
        assertFalse(springCourses.isEmpty(), "Spring Boot courses should be found");

        // 3. Filter Courses by Difficulty (BEGINNER) and isFree (true)
        Page<CourseResponse> freeBeginnerCourses = courseService.filterCourses(
                CourseDifficulty.BEGINNER, null, null, true, null, PageRequest.of(0, 10)
        );
        assertFalse(freeBeginnerCourses.getContent().isEmpty(), "Should find free beginner courses");

        // 4. Test Course-Skill mappings
        CourseResponse springBootCourse = springCourses.get(0);
        List<CourseSkillResponse> courseSkills = courseSkillService.getCourseSkills(springBootCourse.id());
        assertFalse(courseSkills.isEmpty(), "Course should have mapped skills");

        // 5. Test Skill-Courses Lookup ("Spring Boot" skill -> courses)
        List<SkillResponse> springBootSkillList = skillService.searchSkillsByName("Spring Boot");
        assertFalse(springBootSkillList.isEmpty());
        SkillResponse springBootSkill = springBootSkillList.get(0);

        List<CourseResponse> coursesTeachingSpringBoot = courseSkillService.getCoursesBySkillId(springBootSkill.id());
        assertFalse(coursesTeachingSpringBoot.isEmpty(), "Should find courses teaching Spring Boot");
    }
}
