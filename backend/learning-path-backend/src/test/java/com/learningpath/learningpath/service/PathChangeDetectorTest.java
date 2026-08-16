package com.learningpath.learningpath.service;

import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.entity.UserProgress;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PathChangeDetectorTest {

    private PathChangeDetector detector;

    private UUID userId;
    private UUID careerId;

    @BeforeEach
    void setUp() {
        detector = new PathChangeDetector();
        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();
    }

    @Test
    void detect_alwaysAdapts_whenCurrentPathIsNull() {
        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of("java"), Set.of("spring", "rest apis"), List.of()
        );

        PathChangeDecision decision = detector.detect(snapshot, null);

        assertThat(decision.shouldAdapt()).isTrue();
        assertThat(decision.reason()).containsIgnoringCase("No existing");
    }

    @Test
    void detect_alwaysAdapts_whenCurrentPathHasNoPhases() {
        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of(), Set.of("java"), List.of()
        );

        PersonalizedLearningPathResponse emptyPath = PersonalizedLearningPathResponse.ok(
                userId, "Software Engineer", "summary", List.of(), "TEST", "test-model"
        );

        PathChangeDecision decision = detector.detect(snapshot, emptyPath);

        assertThat(decision.shouldAdapt()).isTrue();
    }

    @Test
    void detect_adapts_whenCareerGoalChanged() {
        UUID course1Id = UUID.randomUUID();
        PersonalizedLearningPathResponse oldPath = buildPathWithCourse("Data Scientist", course1Id, "Data Science 101");

        // Snapshot has a DIFFERENT career goal
        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of(), Set.of("java"), List.of()
        );

        PathChangeDecision decision = detector.detect(snapshot, oldPath);

        assertThat(decision.shouldAdapt()).isTrue();
        assertThat(decision.reason()).containsIgnoringCase("Career goal changed");
    }

    @Test
    void detect_adapts_whenAllPathCoursesCompleted() {
        UUID courseId = UUID.randomUUID();
        PersonalizedLearningPathResponse path = buildPathWithCourse("Software Engineer", courseId, "Java Fundamentals");

        // The course in the path is marked COMPLETED
        Course course = Course.builder().title("Java Fundamentals").build();
        setId(course, courseId);

        User user = User.builder().email("a@b.com").fullName("Test").build();
        UserProgress completedProgress = UserProgress.builder()
                .user(user)
                .course(course)
                .status(ProgressStatus.COMPLETED)
                .completionPercentage(BigDecimal.valueOf(100))
                .build();

        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of(), Set.of("java"), List.of(completedProgress)
        );

        PathChangeDecision decision = detector.detect(snapshot, path);

        assertThat(decision.shouldAdapt()).isTrue();
        assertThat(decision.reason()).containsIgnoringCase("completed");
    }

    @Test
    void detect_adapts_whenSomePathCourseCompleted() {
        UUID courseId1 = UUID.randomUUID();
        UUID courseId2 = UUID.randomUUID();

        // Path has 2 courses, only one is completed
        PersonalizedLearningPathResponse path = buildPathWithCourses("Software Engineer",
                List.of(courseId1, courseId2), List.of("Course A", "Course B"));

        Course completedCourse = Course.builder().title("Course A").build();
        setId(completedCourse, courseId1);

        User user = User.builder().email("a@b.com").fullName("Test").build();
        UserProgress completedProgress = UserProgress.builder()
                .user(user)
                .course(completedCourse)
                .status(ProgressStatus.COMPLETED)
                .completionPercentage(BigDecimal.valueOf(100))
                .build();

        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of(), Set.of("java"), List.of(completedProgress)
        );

        PathChangeDecision decision = detector.detect(snapshot, path);

        assertThat(decision.shouldAdapt()).isTrue();
        assertThat(decision.reason()).containsIgnoringCase("completed");
    }

    @Test
    void detect_noChange_whenNoCoursesCompleted() {
        UUID courseId = UUID.randomUUID();
        PersonalizedLearningPathResponse path = buildPathWithCourse("Software Engineer", courseId, "Java Basics");

        // No courses completed
        LearnerSnapshot snapshot = new LearnerSnapshot(
                userId, careerId, "Software Engineer",
                Set.of(), Set.of("java"), List.of()
        );

        PathChangeDecision decision = detector.detect(snapshot, path);

        assertThat(decision.shouldAdapt()).isFalse();
        assertThat(decision.reason()).containsIgnoringCase("No meaningful");
    }

    @Test
    void noChange_factoryMethod_returnsFalse() {
        PathChangeDecision decision = PathChangeDecision.noChange();
        assertThat(decision.shouldAdapt()).isFalse();
    }

    @Test
    void adapt_factoryMethod_returnsTrue() {
        PathChangeDecision decision = PathChangeDecision.adapt("Course completed");
        assertThat(decision.shouldAdapt()).isTrue();
        assertThat(decision.reason()).isEqualTo("Course completed");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PersonalizedLearningPathResponse buildPathWithCourse(String career, UUID courseId, String title) {
        return buildPathWithCourses(career, List.of(courseId), List.of(title));
    }

    private PersonalizedLearningPathResponse buildPathWithCourses(String career, List<UUID> courseIds, List<String> titles) {
        List<RecommendedCourseItem> courses = new java.util.ArrayList<>();
        for (int i = 0; i < courseIds.size(); i++) {
            courses.add(new RecommendedCourseItem(courseIds.get(i), titles.get(i), "Provider", 0.9, "BEGINNER", List.of()));
        }
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("Skill A"), courses, "2 weeks", "Foundation");
        return PersonalizedLearningPathResponse.ok(userId, career, "summary", List.of(phase), "TEST", "test-model");
    }

    /**
     * Reflection helper to set the ID field on a BaseEntity subclass (since @Id is set by JPA normally).
     */
    private void setId(Object entity, UUID id) {
        try {
            var field = com.learningpath.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID via reflection: " + e.getMessage(), e);
        }
    }
}
