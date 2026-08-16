package com.learningpath.learningpath.service;

import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnerStateServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private CareerSkillRepository careerSkillRepository;
    @Mock private UserSkillRepository userSkillRepository;
    @Mock private UserProgressRepository userProgressRepository;

    private LearnerStateService service;

    private UUID userId;
    private UUID careerId;

    @BeforeEach
    void setUp() {
        service = new LearnerStateService(
                userRepository, careerRepository, careerSkillRepository,
                userSkillRepository, userProgressRepository
        );
        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();
    }

    @Test
    void computeCompletedSkills_returnsAdvancedAndExpertSkills() {
        Skill javaSkill = buildSkill("Java");
        Skill pySkill = buildSkill("Python");
        Skill jsSkill = buildSkill("JavaScript");

        List<UserSkill> userSkills = List.of(
                buildUserSkill(javaSkill, ProficiencyLevel.ADVANCED),
                buildUserSkill(pySkill, ProficiencyLevel.EXPERT),
                buildUserSkill(jsSkill, ProficiencyLevel.BEGINNER) // not mastered
        );

        when(userSkillRepository.findByUserId(userId)).thenReturn(userSkills);

        Set<String> completed = service.computeCompletedSkills(userId);

        assertThat(completed).containsExactlyInAnyOrder("java", "python");
        assertThat(completed).doesNotContain("javascript");
    }

    @Test
    void computeCompletedSkills_returnsEmptySet_whenNoMasteredSkills() {
        Skill skill = buildSkill("Docker");
        when(userSkillRepository.findByUserId(userId))
                .thenReturn(List.of(buildUserSkill(skill, ProficiencyLevel.BEGINNER)));

        Set<String> completed = service.computeCompletedSkills(userId);

        assertThat(completed).isEmpty();
    }

    @Test
    void computeRemainingSkills_returnsSkillsNotInCompletedSet() {
        Skill java = buildSkill("Java");
        Skill spring = buildSkill("Spring Boot");
        Skill python = buildSkill("Python");

        CareerSkill cs1 = buildCareerSkill(java);
        CareerSkill cs2 = buildCareerSkill(spring);
        CareerSkill cs3 = buildCareerSkill(python);

        when(careerSkillRepository.findByCareerId(careerId)).thenReturn(List.of(cs1, cs2, cs3));

        Set<String> completed = Set.of("java"); // Java is mastered
        Set<String> remaining = service.computeRemainingSkills(careerId, completed);

        assertThat(remaining).containsExactlyInAnyOrder("spring boot", "python");
        assertThat(remaining).doesNotContain("java");
    }

    @Test
    void computeRemainingSkills_returnsEmpty_whenAllMastered() {
        Skill java = buildSkill("Java");
        CareerSkill cs = buildCareerSkill(java);
        when(careerSkillRepository.findByCareerId(careerId)).thenReturn(List.of(cs));

        Set<String> completed = Set.of("java");
        Set<String> remaining = service.computeRemainingSkills(careerId, completed);

        assertThat(remaining).isEmpty();
    }

    @Test
    void snapshot_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.snapshot(userId, careerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void snapshot_throwsResourceNotFoundException_whenCareerNotFound() {
        com.learningpath.entity.User user = com.learningpath.entity.User.builder()
                .email("x@x.com").fullName("Test").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(careerRepository.findById(careerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.snapshot(userId, careerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(careerId.toString());
    }

    @Test
    void snapshot_returnsCorrectLearnerSnapshot() {
        com.learningpath.entity.User user = com.learningpath.entity.User.builder()
                .email("a@b.com").fullName("Alice").build();
        Career career = Career.builder().title("Data Scientist").build();

        Skill pythonSkill = buildSkill("Python");
        Skill mlSkill = buildSkill("Machine Learning");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(careerRepository.findById(careerId)).thenReturn(Optional.of(career));
        when(userSkillRepository.findByUserId(userId))
                .thenReturn(List.of(buildUserSkill(pythonSkill, ProficiencyLevel.ADVANCED)));
        when(careerSkillRepository.findByCareerId(careerId))
                .thenReturn(List.of(buildCareerSkill(pythonSkill), buildCareerSkill(mlSkill)));
        when(userProgressRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        LearnerSnapshot snapshot = service.snapshot(userId, careerId);

        assertThat(snapshot.userId()).isEqualTo(userId);
        assertThat(snapshot.careerId()).isEqualTo(careerId);
        assertThat(snapshot.targetCareer()).isEqualTo("Data Scientist");
        assertThat(snapshot.completedSkills()).containsExactly("python");
        assertThat(snapshot.remainingSkills()).containsExactly("machine learning");
        assertThat(snapshot.courseProgress()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Skill buildSkill(String name) {
        return Skill.builder().name(name).build();
    }

    private UserSkill buildUserSkill(Skill skill, ProficiencyLevel level) {
        return UserSkill.builder()
                .skill(skill)
                .proficiencyLevel(level)
                .source(SkillSource.SELF_REPORTED)
                .build();
    }

    private CareerSkill buildCareerSkill(Skill skill) {
        return CareerSkill.builder()
                .skill(skill)
                .requiredProficiency(ProficiencyLevel.INTERMEDIATE)
                .build();
    }
}
