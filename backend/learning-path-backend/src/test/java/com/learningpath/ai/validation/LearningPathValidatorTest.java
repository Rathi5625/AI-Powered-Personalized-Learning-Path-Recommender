package com.learningpath.ai.validation;

import com.learningpath.learningpath.dto.LearningPathContext;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import com.learningpath.skilldependency.dto.PrerequisitesResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPathValidatorTest {

    @Mock
    private SkillDependencyService dependencyService;

    private LearningPathValidator validator;

    private UUID userId;
    private UUID courseId1;
    private UUID courseId2;
    private RecommendedCourseItem course1;
    private RecommendedCourseItem course2;
    private LearningPathContext defaultContext;

    @BeforeEach
    void setUp() {
        validator = new LearningPathValidator(dependencyService);

        userId = UUID.randomUUID();
        courseId1 = UUID.randomUUID();
        courseId2 = UUID.randomUUID();

        course1 = new RecommendedCourseItem(courseId1, "HTML & CSS Foundations", "Udemy", 0.95, "BEGINNER", List.of("HTML", "CSS"));
        course2 = new RecommendedCourseItem(courseId2, "JavaScript Complete Masterclass", "Coursera", 0.90, "INTERMEDIATE", List.of("JavaScript"));

        defaultContext = new LearningPathContext(
                userId,
                "Alice Learner",
                "Frontend Developer",
                "BEGINNER",
                "VISUAL",
                2.0,
                List.of("Internet Basics"),
                List.of("HTML", "CSS", "JavaScript"),
                List.of("HTML", "CSS", "JavaScript"),
                List.of(course1, course2)
        );

        org.mockito.Mockito.lenient().when(dependencyService.getPrerequisites(anyString()))
                .thenReturn(PrerequisitesResponse.unknown("none"));
    }

    // 1. Valid learning path
    @Test
    void testValidateResponse_ValidPath_Success() {
        when(dependencyService.getPrerequisites(anyString()))
                .thenReturn(PrerequisitesResponse.unknown("none"));

        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        LearningPathPhase phase2 = new LearningPathPhase(2, "Phase 2", List.of("JavaScript"), List.of(course2), "3 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Valid summary", List.of(phase1, phase2), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    // 2. Null response
    @Test
    void testValidateResponse_NullResponse_ReturnsInvalid() {
        ValidationResult result = validator.validateResponse(null, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().contains("AI response cannot be null"));
    }

    // 3. Empty phases
    @Test
    void testValidateResponse_EmptyPhases_ReturnsInvalid() {
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", Collections.emptyList(), "GEMINI", "gemini-1.5-flash"
        );
        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("no phases")));
    }

    // 4. Unknown course ID (Grounding Violation)
    @Test
    void testValidateResponse_UnknownCourseId_ReturnsInvalid() {
        UUID fakeId = UUID.randomUUID();
        RecommendedCourseItem fakeCourse = new RecommendedCourseItem(fakeId, "Fake Course", "Udemy", 0.5, "BEGINNER", List.of("HTML"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(fakeCourse), "1 week", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Grounding Violation")));
    }

    // 5. Incorrect course title
    @Test
    void testValidateResponse_IncorrectCourseTitle_ReturnsInvalid() {
        RecommendedCourseItem tamperedTitle = new RecommendedCourseItem(courseId1, "Tampered Title", "Udemy", 0.95, "BEGINNER", List.of("HTML"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(tamperedTitle), "1 week", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Title Mismatch")));
    }

    // 6. Incorrect provider
    @Test
    void testValidateResponse_IncorrectProvider_ReturnsInvalid() {
        RecommendedCourseItem tamperedProvider = new RecommendedCourseItem(courseId1, "HTML & CSS Foundations", "Coursera", 0.95, "BEGINNER", List.of("HTML"));
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(tamperedProvider), "1 week", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Provider Mismatch")));
    }

    // 7. Unknown skill / empty target skills
    @Test
    void testValidateResponse_EmptyTargetSkills_ReturnsInvalid() {
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", Collections.emptyList(), List.of(course1), "1 week", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("no target skills")));
    }

    // 8. Invalid prerequisite order
    @Test
    void testValidateResponse_PrerequisiteViolation_ReturnsInvalid() {
        // JS in phase 1, HTML in phase 2, but JS requires HTML
        when(dependencyService.getPrerequisites("JavaScript"))
                .thenReturn(new PrerequisitesResponse("JavaScript", true, List.of("HTML"), List.of("HTML")));

        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1", List.of("JavaScript"), List.of(course2), "2 weeks", "Exp");
        LearningPathPhase phase2 = new LearningPathPhase(2, "Phase 2", List.of("HTML"), List.of(course1), "2 weeks", "Exp");

        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase1, phase2), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Prerequisite violation")));
    }

    // 9. Duplicate course
    @Test
    void testValidateResponse_DuplicateCourse_ReturnsInvalid() {
        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        LearningPathPhase phase2 = new LearningPathPhase(2, "Phase 2", List.of("CSS"), List.of(course1), "2 weeks", "Exp");

        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase1, phase2), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Duplicate course ID detected")));
    }

    // 10. Invalid duration
    @Test
    void testValidateResponse_NegativeDuration_ReturnsInvalid() {
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "-3 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("non-positive duration")));
    }

    // 11. Wrong career goal
    @Test
    void testValidateResponse_WrongCareerGoal_ReturnsInvalid() {
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Data Scientist", "Summary", List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Generated career goal")));
    }

    // 12. Missing required fields in context
    @Test
    void testValidateContext_MissingUserId_ReturnsInvalid() {
        LearningPathContext badContext = new LearningPathContext(
                null, "Alice", "Frontend", "BEGINNER", "VISUAL", 1.0, List.of(), List.of(), List.of(), List.of(course1)
        );
        ValidationResult result = validator.validateContext(badContext);
        assertFalse(result.valid());
        assertTrue(result.errors().contains("Learner userId is missing from context"));
    }

    // 14. Excessive response size (>50,000 chars)
    @Test
    void testValidateResponse_ExcessiveResponseSize_ReturnsInvalid() {
        String longSummary = "A".repeat(50001);
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", longSummary, List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("exceeds maximum allowed size")));
    }

    // 15. Sensitive data protection
    @Test
    void testValidateResponse_SensitiveTokenLeak_ReturnsInvalid() {
        String summaryWithSecret = "Here is your path. Secret: AIzaSyD123456789";
        LearningPathPhase phase = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", summaryWithSecret, List.of(phase), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("prohibited sensitive pattern token")));
    }

    // 23. Valid path when prerequisites already satisfied in current skills
    @Test
    void testValidateResponse_PrerequisiteAlreadySatisfied_Success() {
        // Learner already knows Internet Basics and HTML
        LearningPathContext contextWithKnownSkills = new LearningPathContext(
                userId, "Alice", "Frontend Developer", "BEGINNER", "VISUAL", 2.0,
                List.of("HTML"), List.of("JavaScript"), List.of("JavaScript"), List.of(course2)
        );

        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1", List.of("JavaScript"), List.of(course2), "2 weeks", "Exp");
        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase1), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, contextWithKnownSkills);
        assertTrue(result.valid());
    }

    // 24. Duplicate phase numbers
    @Test
    void testValidateResponse_DuplicatePhaseNumbers_ReturnsInvalid() {
        LearningPathPhase phase1 = new LearningPathPhase(1, "Phase 1", List.of("HTML"), List.of(course1), "2 weeks", "Exp");
        LearningPathPhase phase2 = new LearningPathPhase(1, "Phase 2", List.of("JavaScript"), List.of(course2), "2 weeks", "Exp");

        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId, "Frontend Developer", "Summary", List.of(phase1, phase2), "GEMINI", "gemini-1.5-flash"
        );

        ValidationResult result = validator.validateResponse(response, defaultContext);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Duplicate phase number detected")));
    }
}
