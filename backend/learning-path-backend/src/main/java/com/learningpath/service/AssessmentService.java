package com.learningpath.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.AssessmentDto;
import com.learningpath.dto.AssessmentQuestionDto;
import com.learningpath.dto.AssessmentResultDto;
import com.learningpath.dto.AssessmentSubmissionRequest;
import com.learningpath.entity.Assessment;
import com.learningpath.entity.AssessmentQuestion;
import com.learningpath.entity.AssessmentResult;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.ActivityType;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.AssessmentQuestionRepository;
import com.learningpath.repository.AssessmentRepository;
import com.learningpath.repository.AssessmentResultRepository;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentResultRepository resultRepository;
    private final UserRepository userRepository;
    private final LearningActivityService activityService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<AssessmentDto> getAllAssessments() {
        List<Assessment> assessments = assessmentRepository.findAll();
        return assessments.stream()
                .map(this::mapToSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentDto getAssessmentById(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId));

        List<AssessmentQuestion> questions = questionRepository.findAllByAssessmentId(assessmentId);
        List<AssessmentQuestionDto> questionDtos = questions.stream()
                .map(this::mapQuestionToDto)
                .toList();

        return new AssessmentDto(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getDescription(),
                assessment.getSkill().getId(),
                assessment.getSkill().getName(),
                assessment.getPassingScore(),
                questionDtos.size(),
                Math.max(10, questionDtos.size() * 2),
                questionDtos
        );
    }

    @Transactional
    public AssessmentResultDto submitAssessment(UUID userId, UUID assessmentId, AssessmentSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found"));

        List<AssessmentQuestion> questions = questionRepository.findAllByAssessmentId(assessmentId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Assessment has no questions");
        }

        int totalQuestions = questions.size();
        int correctCount = 0;
        int totalPoints = 0;
        int earnedPoints = 0;

        Map<UUID, String> submittedAnswers = request.answers() != null ? request.answers() : Collections.emptyMap();

        for (AssessmentQuestion q : questions) {
            int points = q.getPoints() != null ? q.getPoints() : 10;
            totalPoints += points;

            String submitted = submittedAnswers.get(q.getId());
            if (submitted != null && submitted.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                correctCount++;
                earnedPoints += points;
            }
        }

        BigDecimal percentageScore = totalPoints > 0
                ? BigDecimal.valueOf((double) earnedPoints / totalPoints * 100).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean passed = percentageScore.compareTo(BigDecimal.valueOf(assessment.getPassingScore())) >= 0;

        ProficiencyLevel proficiency;
        if (percentageScore.compareTo(BigDecimal.valueOf(85)) >= 0) {
            proficiency = ProficiencyLevel.ADVANCED;
        } else if (percentageScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            proficiency = ProficiencyLevel.INTERMEDIATE;
        } else {
            proficiency = ProficiencyLevel.BEGINNER;
        }

        AssessmentResult result = AssessmentResult.builder()
                .user(user)
                .assessment(assessment)
                .score(percentageScore)
                .passed(passed)
                .evaluatedProficiency(proficiency)
                .completedAt(Instant.now())
                .build();

        AssessmentResult saved = resultRepository.save(result);
        log.info("[AssessmentService] Evaluated assessment id={}, user={}, score={}, passed={}",
                assessmentId, userId, percentageScore, passed);

        // Log learner activity telemetry for future ML
        activityService.logActivity(
                userId,
                ActivityType.ASSESSMENT_SUBMIT,
                "ASSESSMENT",
                assessmentId.toString(),
                "score=" + percentageScore + ",passed=" + passed,
                request.timeSpentSeconds()
        );

        return new AssessmentResultDto(
                saved.getId(),
                assessment.getId(),
                assessment.getTitle(),
                assessment.getSkill().getName(),
                saved.getScore(),
                saved.isPassed(),
                saved.getEvaluatedProficiency(),
                totalQuestions,
                correctCount,
                request.timeSpentSeconds(),
                saved.getCompletedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AssessmentResultDto> getUserAssessmentResults(UUID userId) {
        return resultRepository.findAllByUserIdOrderByCompletedAtDesc(userId).stream()
                .map(r -> new AssessmentResultDto(
                        r.getId(),
                        r.getAssessment().getId(),
                        r.getAssessment().getTitle(),
                        r.getAssessment().getSkill().getName(),
                        r.getScore(),
                        r.isPassed(),
                        r.getEvaluatedProficiency(),
                        0,
                        0,
                        null,
                        r.getCompletedAt()
                ))
                .toList();
    }

    private AssessmentDto mapToSummaryDto(Assessment a) {
        List<AssessmentQuestion> questions = questionRepository.findAllByAssessmentId(a.getId());
        return new AssessmentDto(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getSkill().getId(),
                a.getSkill().getName(),
                a.getPassingScore(),
                questions.size(),
                Math.max(10, questions.size() * 2),
                null
        );
    }

    private AssessmentQuestionDto mapQuestionToDto(AssessmentQuestion q) {
        List<String> options = new ArrayList<>();
        if (q.getOptionsJson() != null && !q.getOptionsJson().isBlank()) {
            try {
                options = objectMapper.readValue(q.getOptionsJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
                options = Arrays.stream(q.getOptionsJson().split(";"))
                        .map(String::trim)
                        .toList();
            }
        }

        return new AssessmentQuestionDto(
                q.getId(),
                q.getQuestionText(),
                q.getQuestionType(),
                q.getDifficulty(),
                options,
                q.getPoints()
        );
    }
}
