package com.learningpath.skill;

import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.SkillAlias;
import com.learningpath.entity.enums.SkillMappingType;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillAliasRepository;
import com.learningpath.repository.SkillRepository;
import com.learningpath.service.SkillMappingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_skill_mapping;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class SkillMappingIntegrationTest {

    @Autowired
    private SkillMappingService skillMappingService;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillAliasRepository skillAliasRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSkillRepository courseSkillRepository;

    @Test
    @DisplayName("1. Verify Canonical Skills Synchronized in Database")
    void testCanonicalSkillsExist() {
        long skillCount = skillRepository.count();
        // 25 baseline + 40 additional DAG skills = 65 canonical skills
        assertThat(skillCount).isGreaterThanOrEqualTo(65);

        assertThat(skillRepository.findByName("HTML")).isPresent();
        assertThat(skillRepository.findByName("CSS")).isPresent();
        assertThat(skillRepository.findByName("JavaScript")).isPresent();
        assertThat(skillRepository.findByName("Building for Scale")).isPresent();
        assertThat(skillRepository.findByName("Message Brokers")).isPresent();
        assertThat(skillRepository.findByName("Caching")).isPresent();
    }

    @Test
    @DisplayName("2. Verify Exact Skill Mappings")
    void testExactSkillMappings() {
        Optional<Skill> htmlSkill = skillMappingService.resolveCanonicalSkill("HTML");
        assertThat(htmlSkill).isPresent();
        assertThat(htmlSkill.get().getName()).isEqualTo("HTML");

        Optional<Skill> scaleSkill = skillMappingService.resolveCanonicalSkill("Building for Scale");
        assertThat(scaleSkill).isPresent();
        assertThat(scaleSkill.get().getName()).isEqualTo("Building for Scale");

        Optional<Skill> brokerSkill = skillMappingService.resolveCanonicalSkill("Message Brokers");
        assertThat(brokerSkill).isPresent();
        assertThat(brokerSkill.get().getName()).isEqualTo("Message Brokers");
    }

    @Test
    @DisplayName("3. Verify Alias Skill Mappings")
    void testAliasSkillMappings() {
        // CSS Fundamentals -> CSS
        Optional<Skill> cssOpt = skillMappingService.resolveCanonicalSkill("CSS Fundamentals");
        assertThat(cssOpt).isPresent();
        assertThat(cssOpt.get().getName()).isEqualTo("CSS");

        // HTML Fundamentals -> HTML
        Optional<Skill> htmlOpt = skillMappingService.resolveCanonicalSkill("HTML Fundamentals");
        assertThat(htmlOpt).isPresent();
        assertThat(htmlOpt.get().getName()).isEqualTo("HTML");

        // Internet Fundamentals -> Internet Basics
        Optional<Skill> netOpt = skillMappingService.resolveCanonicalSkill("Internet Fundamentals");
        assertThat(netOpt).isPresent();
        assertThat(netOpt.get().getName()).isEqualTo("Internet Basics");

        // JavaScript Foundations -> JavaScript
        Optional<Skill> jsOpt = skillMappingService.resolveCanonicalSkill("JavaScript Foundations");
        assertThat(jsOpt).isPresent();
        assertThat(jsOpt.get().getName()).isEqualTo("JavaScript");

        // Python Basics -> Python
        Optional<Skill> pyOpt = skillMappingService.resolveCanonicalSkill("Python Basics");
        assertThat(pyOpt).isPresent();
        assertThat(pyOpt.get().getName()).isEqualTo("Python");
    }

    @Test
    @DisplayName("4. Verify SkillAlias Persistent Table Records")
    void testSkillAliasTable() {
        long aliasCount = skillAliasRepository.count();
        // 56 Exact + 5 Alias = 61 persistent aliases
        assertThat(aliasCount).isEqualTo(61);

        Optional<SkillAlias> cssAlias = skillAliasRepository.findByDatasetSkillNameIgnoreCase("CSS Fundamentals");
        assertThat(cssAlias).isPresent();
        assertThat(cssAlias.get().getMappingType()).isEqualTo(SkillMappingType.ALIAS);
        assertThat(cssAlias.get().getConfidence()).isEqualTo(1.00);
        assertThat(cssAlias.get().getCanonicalSkill().getName()).isEqualTo("CSS");

        Optional<SkillAlias> exactAlias = skillAliasRepository.findByDatasetSkillNameIgnoreCase("Building for Scale");
        assertThat(exactAlias).isPresent();
        assertThat(exactAlias.get().getMappingType()).isEqualTo(SkillMappingType.EXACT);
        assertThat(exactAlias.get().getCanonicalSkill().getName()).isEqualTo("Building for Scale");
    }

    @Test
    @DisplayName("5. Verify All 244 Dataset Courses Are Linked to Canonical Skills")
    void testCourseSkillLinking() {
        List<Course> datasetCourses = courseRepository.findAll().stream()
                .filter(c -> c.getCourseCode() != null)
                .toList();

        assertThat(datasetCourses).hasSize(244);

        for (Course course : datasetCourses) {
            List<CourseSkill> mappings = courseSkillRepository.findByCourseId(course.getId());
            assertThat(mappings)
                    .withFailMessage("Course %s (%s) has no CourseSkill mapping!", course.getCourseCode(), course.getTitle())
                    .isNotEmpty();

            CourseSkill primarySkill = mappings.get(0);
            assertThat(primarySkill.getSkill()).isNotNull();
            assertThat(primarySkill.getSkill().getName()).isNotBlank();
        }
    }

    @Test
    @DisplayName("6. Idempotency: Re-running CourseSkill linking creates 0 duplicates")
    void testLinkingIdempotency() {
        long countBefore = courseSkillRepository.count();

        SkillMappingService.CourseLinkingSummary summary = skillMappingService.linkAllCuratedCourses();
        assertThat(summary.newlyLinked()).isEqualTo(0);
        assertThat(summary.alreadyLinked()).isEqualTo(244);
        assertThat(summary.unresolved()).isEqualTo(0);

        long countAfter = courseSkillRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    @DisplayName("7. Recommendation Candidate Compatibility: Candidate lookup by canonical skill ID")
    void testCandidateLookupBySkillId() {
        Skill htmlSkill = skillRepository.findByName("HTML").orElseThrow();
        List<CourseSkill> htmlCourseSkills = courseSkillRepository.findBySkillIdIn(List.of(htmlSkill.getId()));

        // HTML has baseline course + 4 dataset courses (HTML) + 4 dataset courses (HTML Fundamentals)
        assertThat(htmlCourseSkills).hasSizeGreaterThanOrEqualTo(8);

        // Verify HTML course codes are present
        List<String> codes = htmlCourseSkills.stream()
                .map(cs -> cs.getCourse().getCourseCode())
                .filter(c -> c != null)
                .toList();

        assertThat(codes).contains("FE_02_01", "FE_02_02", "FE_02_03", "FE_02_04");
    }
}
