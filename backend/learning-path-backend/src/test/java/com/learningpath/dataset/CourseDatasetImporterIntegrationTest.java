package com.learningpath.dataset;

import com.learningpath.entity.Course;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_dataset_importer;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class CourseDatasetImporterIntegrationTest {

    @Autowired
    private CourseDatasetImporter importer;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("1. Verify dataset courses exist and have correct properties")
    void testDatasetCoursesProperties() {
        long totalCourses = courseRepository.count();
        // At startup, 21 baseline courses + 244 dataset courses = 265
        assertThat(totalCourses).isGreaterThanOrEqualTo(265);

        // Verify FE_02_01 (HTML Beginner)
        Optional<Course> fe0201Opt = courseRepository.findByCourseCode("FE_02_01");
        assertThat(fe0201Opt).isPresent();
        Course fe0201 = fe0201Opt.get();
        assertThat(fe0201.getTitle()).isEqualTo("MDN: Structuring content with HTML");
        assertThat(fe0201.getDifficulty()).isEqualTo(CourseDifficulty.BEGINNER);
        assertThat(fe0201.getProvider()).isEqualTo("MDN");
        assertThat(fe0201.getUrl()).isEqualTo("https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Structuring_content");
        assertThat(fe0201.getDurationHours()).isEqualTo(3.0);
        assertThat(fe0201.getCourseType()).isEqualTo(CourseType.DOCUMENTATION);
        assertThat(fe0201.isFree()).isTrue();

        // Verify BE_29_04 (Building for Scale High)
        Optional<Course> be2904Opt = courseRepository.findByCourseCode("BE_29_04");
        assertThat(be2904Opt).isPresent();
        Course be2904 = be2904Opt.get();
        assertThat(be2904.getTitle()).isEqualTo("Designing Data-Intensive Applications resources");
        assertThat(be2904.getDifficulty()).isEqualTo(CourseDifficulty.HIGH);
        assertThat(be2904.getDurationHours()).isEqualTo(15.0);
        assertThat(be2904.getProvider()).isEqualTo("O'Reilly");
    }

    @Test
    @DisplayName("2. Idempotency Test: Running importer when data exists results in 0 duplicates")
    void testIdempotentExecution() {
        long countBefore = courseRepository.count();

        ImportSummary summary = importer.importDataset();
        assertThat(summary.totalSourceRows()).isEqualTo(244);
        assertThat(summary.validRows()).isEqualTo(244);
        assertThat(summary.importedCourses()).isEqualTo(0);
        assertThat(summary.skippedDuplicates()).isEqualTo(244);
        assertThat(summary.invalidRows()).isEqualTo(0);

        long countAfter = courseRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Autowired
    private CourseSkillRepository courseSkillRepository;

    @Test
    @DisplayName("3. Clean Import Test: Fresh import ingests all 244 courses")
    void testCleanImport() {
        // Clear course skills first to satisfy referential integrity
        courseSkillRepository.deleteAll();
        courseRepository.deleteAll();
        assertThat(courseRepository.count()).isEqualTo(0);

        ImportSummary summary = importer.importDataset();
        assertThat(summary.totalSourceRows()).isEqualTo(244);
        assertThat(summary.validRows()).isEqualTo(244);
        assertThat(summary.importedCourses()).isEqualTo(244);
        assertThat(summary.skippedDuplicates()).isEqualTo(0);
        assertThat(summary.invalidRows()).isEqualTo(0);
        assertThat(courseRepository.count()).isEqualTo(244);
    }

    @Test
    @DisplayName("4. Verify unresolved skill tracking for Step 4")
    void testUnresolvedSkillReporting() {
        ImportSummary summary = importer.importDataset();
        assertThat(summary.unresolvedSkills()).isNotEmpty();
        assertThat(summary.unresolvedSkills()).contains("CSS Fundamentals", "HTML Fundamentals", "Internet Fundamentals");
    }
}
