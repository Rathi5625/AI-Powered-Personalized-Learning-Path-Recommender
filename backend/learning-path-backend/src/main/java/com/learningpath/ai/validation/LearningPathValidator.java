package com.learningpath.ai.validation;

import com.learningpath.learningpath.dto.LearningPathContext;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import com.learningpath.skilldependency.dto.PrerequisitesResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningPathValidator {

    private static final int MAX_RESPONSE_LENGTH = 50000;
    private static final List<String> SENSITIVE_PATTERNS = List.of(
            "AIza", "GEMINI_API_KEY", "DB_PASSWORD", "JWT_SECRET", "access_token"
    );

    private final SkillDependencyService dependencyService;

    public ValidationResult validateContext(LearningPathContext context) {
        List<String> errors = new ArrayList<>();
        if (context == null) {
            return ValidationResult.invalid("Learner context cannot be null");
        }
        if (context.userId() == null) {
            errors.add("Learner userId is missing from context");
        }
        if (context.targetCareer() == null || context.targetCareer().trim().isEmpty()) {
            errors.add("Target career is missing from context");
        }
        if (context.candidateCourses() == null || context.candidateCourses().isEmpty()) {
            errors.add("Candidate recommended courses context is missing or empty");
        }
        return ValidationResult.of(errors, Collections.emptyList());
    }

    public ValidationResult validateResponse(PersonalizedLearningPathResponse response, LearningPathContext context) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (response == null) {
            return ValidationResult.invalid("AI response cannot be null");
        }
        if (!response.success()) {
            return ValidationResult.invalid("AI response indicates failure: " + response.error());
        }
        if (response.phases() == null || response.phases().isEmpty()) {
            return ValidationResult.invalid("AI learning path response contains no phases");
        }

        // 1. Response Size & Content Check
        String responseSummary = response.summary() != null ? response.summary() : "";
        if (responseSummary.length() > MAX_RESPONSE_LENGTH) {
            return ValidationResult.invalid("AI response summary exceeds maximum allowed size of " + MAX_RESPONSE_LENGTH + " characters");
        }

        // 2. Career Goal Check
        if (context != null && context.targetCareer() != null) {
            if (response.targetCareer() == null || !response.targetCareer().trim().equalsIgnoreCase(context.targetCareer().trim())) {
                errors.add(String.format("Generated career goal '%s' does not match requested target career '%s'",
                        response.targetCareer(), context.targetCareer()));
            }
        }

        // Candidate course lookup map
        Map<UUID, RecommendedCourseItem> candidateMap = (context != null && context.candidateCourses() != null)
                ? context.candidateCourses().stream().collect(Collectors.toMap(RecommendedCourseItem::courseId, c -> c, (a, b) -> a))
                : Collections.emptyMap();

        Set<UUID> seenCourseIds = new HashSet<>();
        Map<String, Integer> skillPhaseMap = new HashMap<>(); // skill -> earliest phase index

        // Current learner skills normalized
        Set<String> knownLearnerSkills = (context != null && context.currentSkills() != null)
                ? context.currentSkills().stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Collections.emptySet();

        // Pre-populate skill to earliest phase map
        for (LearningPathPhase phase : response.phases()) {
            if (phase.targetSkills() != null) {
                for (String skill : phase.targetSkills()) {
                    if (skill != null && !skill.trim().isEmpty()) {
                        skillPhaseMap.putIfAbsent(skill.trim().toLowerCase(), phase.phaseNumber());
                    }
                }
            }
        }

        int expectedPhaseNumber = 1;
        Set<Integer> seenPhaseNumbers = new HashSet<>();

        for (LearningPathPhase phase : response.phases()) {
            // 3. Phase Sequence Validation
            if (phase.phaseNumber() <= 0) {
                errors.add("Phase number must be positive: " + phase.phaseNumber());
            }
            if (!seenPhaseNumbers.add(phase.phaseNumber())) {
                errors.add("Duplicate phase number detected: " + phase.phaseNumber());
            }
            if (phase.phaseNumber() != expectedPhaseNumber) {
                warnings.add(String.format("Non-sequential phase numbering: expected %d, got %d", expectedPhaseNumber, phase.phaseNumber()));
            }
            expectedPhaseNumber++;

            if (phase.targetSkills() == null || phase.targetSkills().isEmpty()) {
                errors.add("Phase " + phase.phaseNumber() + " contains no target skills");
            }
            if (phase.courses() == null || phase.courses().isEmpty()) {
                errors.add("Phase " + phase.phaseNumber() + " contains no courses");
            }

            // 4. Duration Validation
            validateDuration(phase.estimatedDuration(), phase.phaseNumber(), errors, warnings);

            // 5. Skill & Prerequisite Validation
            if (phase.targetSkills() != null) {
                for (String skill : phase.targetSkills()) {
                    if (skill == null || skill.trim().isEmpty()) continue;

                    // Check prerequisite ordering against earlier skills
                    if (dependencyService != null) {
                        PrerequisitesResponse prereqResp = dependencyService.getPrerequisites(skill);
                        if (prereqResp != null && prereqResp.found() && prereqResp.recursivePrerequisites() != null) {
                            for (String prereq : prereqResp.recursivePrerequisites()) {
                                String lowerPrereq = prereq.toLowerCase();
                                if (!knownLearnerSkills.contains(lowerPrereq)) {
                                    Integer prereqPhase = skillPhaseMap.get(lowerPrereq);
                                    if (prereqPhase == null || prereqPhase > phase.phaseNumber()) {
                                        errors.add(String.format("Prerequisite violation: '%s' (Phase %d) depends on '%s' which appears later in Phase %s or is missing",
                                                skill, phase.phaseNumber(), prereq, (prereqPhase != null ? prereqPhase : "N/A")));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Course Grounding & Field Match Validation
            if (phase.courses() != null) {
                for (RecommendedCourseItem course : phase.courses()) {
                    if (course == null || course.courseId() == null) {
                        errors.add("Phase " + phase.phaseNumber() + " contains a course with missing ID");
                        continue;
                    }

                    // Duplicate Course Check
                    if (!seenCourseIds.add(course.courseId())) {
                        errors.add("Duplicate course ID detected across phases: " + course.courseId());
                    }

                    // Course Grounding Check
                    RecommendedCourseItem canonical = candidateMap.get(course.courseId());
                    if (canonical == null) {
                        errors.add("Course Grounding Violation: Course ID " + course.courseId() + " does not exist in recommendation context");
                    } else {
                        // Title Match Check
                        if (course.courseTitle() != null && !course.courseTitle().trim().equalsIgnoreCase(canonical.courseTitle().trim())) {
                            errors.add(String.format("Course Title Mismatch for ID %s: AI title '%s' does not match canonical title '%s'",
                                    course.courseId(), course.courseTitle(), canonical.courseTitle()));
                        }
                        // Provider Match Check
                        if (course.provider() != null && !course.provider().trim().equalsIgnoreCase(canonical.provider().trim())) {
                            errors.add(String.format("Course Provider Mismatch for ID %s: AI provider '%s' does not match canonical provider '%s'",
                                    course.courseId(), course.provider(), canonical.provider()));
                        }
                    }
                }
            }
        }

        // 7. Sensitive Data Check
        validateSensitiveData(responseSummary, errors);

        return ValidationResult.of(errors, warnings);
    }

    private void validateDuration(String durationStr, int phaseNum, List<String> errors, List<String> warnings) {
        if (durationStr == null || durationStr.trim().isEmpty()) {
            warnings.add("Phase " + phaseNum + " has missing estimated duration");
            return;
        }

        Pattern pattern = Pattern.compile("(-?\\d+)\\s*(week|month|day|hr|hour)s?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(durationStr.trim());
        if (matcher.find()) {
            try {
                int val = Integer.parseInt(matcher.group(1));
                if (val <= 0) {
                    errors.add("Phase " + phaseNum + " has non-positive duration: " + durationStr);
                } else if (val > 104) {
                    errors.add("Phase " + phaseNum + " has unrealistically long duration: " + durationStr);
                }
            } catch (NumberFormatException e) {
                errors.add("Phase " + phaseNum + " contains unparseable numeric duration: " + durationStr);
            }
        }
    }

    private void validateSensitiveData(String text, List<String> errors) {
        if (text == null) return;
        for (String pattern : SENSITIVE_PATTERNS) {
            if (text.contains(pattern)) {
                errors.add("Security Violation: Response contains prohibited sensitive pattern token: " + pattern);
            }
        }
    }
}
