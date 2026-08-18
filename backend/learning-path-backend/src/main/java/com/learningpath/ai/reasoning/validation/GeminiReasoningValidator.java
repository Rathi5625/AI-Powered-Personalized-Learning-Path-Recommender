package com.learningpath.ai.reasoning.validation;

import com.learningpath.ai.reasoning.dto.CandidateCourseDto;
import com.learningpath.ai.reasoning.dto.GeminiCourseExplanationDto;
import com.learningpath.ai.reasoning.dto.GeminiCourseSequenceItemDto;
import com.learningpath.ai.reasoning.dto.GeminiReasoningInput;
import com.learningpath.ai.reasoning.dto.GeminiReasoningResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GeminiReasoningValidator {

    public GeminiReasoningResult validateAndSanitize(GeminiReasoningResult rawResult, GeminiReasoningInput input) {
        if (rawResult == null) {
            return null;
        }

        Set<UUID> validCourseIds = input.candidateCourses().stream()
                .map(CandidateCourseDto::courseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, CandidateCourseDto> candidateMap = input.candidateCourses().stream()
                .filter(c -> c.courseId() != null)
                .collect(Collectors.toMap(CandidateCourseDto::courseId, c -> c, (a, b) -> a));

        // 1. Anti-Hallucination: Validate Recommendations
        List<GeminiCourseExplanationDto> validatedRecs = new ArrayList<>();
        if (rawResult.recommendations() != null) {
            for (GeminiCourseExplanationDto rec : rawResult.recommendations()) {
                if (rec == null || rec.courseId() == null) {
                    continue;
                }

                if (!validCourseIds.contains(rec.courseId())) {
                    log.warn("[GeminiReasoningValidator] Rejected hallucinated courseId '{}' not present in candidates.",
                            rec.courseId());
                    continue;
                }

                CandidateCourseDto candidate = candidateMap.get(rec.courseId());
                List<String> validSkills = (rec.skillsAddressed() != null && !rec.skillsAddressed().isEmpty())
                        ? rec.skillsAddressed()
                        : candidate.skillsCovered();

                List<String> validGaps = (rec.gapSkillsAddressed() != null && !rec.gapSkillsAddressed().isEmpty())
                        ? rec.gapSkillsAddressed()
                        : candidate.gapSkillsAddressed();

                validatedRecs.add(new GeminiCourseExplanationDto(
                        rec.courseId(),
                        rec.reason() != null && !rec.reason().isBlank() ? rec.reason() : "Recommended for career target.",
                        validSkills,
                        validGaps,
                        rec.prerequisiteReason() != null ? rec.prerequisiteReason() : "Valid prerequisite step.",
                        rec.estimatedEffort() != null ? rec.estimatedEffort() : "Self-paced",
                        rec.priority() > 0 ? rec.priority() : validatedRecs.size() + 1
                ));
            }
        }

        // If all recommendations were hallucinated/empty, validation fails
        if (validatedRecs.isEmpty()) {
            log.warn("[GeminiReasoningValidator] All AI recommendations failed validation. Falling back to deterministic reasoning.");
            return null;
        }

        // 2. Anti-Hallucination & Prerequisite Sequencing: Validate Sequence
        List<GeminiCourseSequenceItemDto> validatedSeq = new ArrayList<>();
        if (rawResult.learningSequence() != null) {
            for (GeminiCourseSequenceItemDto item : rawResult.learningSequence()) {
                if (item != null && item.courseId() != null && validCourseIds.contains(item.courseId())) {
                    validatedSeq.add(item);
                }
            }
        }

        // Enforce Prerequisite Graph Consistency on the sequence
        List<GeminiCourseSequenceItemDto> orderedSeq = enforcePrerequisiteSequencing(validatedSeq, candidateMap, input.prerequisiteOrder());

        String summary = (rawResult.summary() != null && !rawResult.summary().isBlank())
                ? rawResult.summary()
                : "Personalized learning path based on your verified skill gaps and prerequisite order.";

        List<String> adaptationNotes = (rawResult.adaptationNotes() != null)
                ? rawResult.adaptationNotes()
                : Collections.emptyList();

        return new GeminiReasoningResult(summary, validatedRecs, orderedSeq, adaptationNotes, true);
    }

    private List<GeminiCourseSequenceItemDto> enforcePrerequisiteSequencing(
            List<GeminiCourseSequenceItemDto> seq,
            Map<UUID, CandidateCourseDto> candidateMap,
            List<String> prerequisiteOrder
    ) {
        if (seq == null || seq.isEmpty()) {
            return Collections.emptyList();
        }

        if (prerequisiteOrder == null || prerequisiteOrder.isEmpty()) {
            return seq;
        }

        // Build skill order index
        Map<String, Integer> skillRankMap = new HashMap<>();
        for (int i = 0; i < prerequisiteOrder.size(); i++) {
            skillRankMap.put(prerequisiteOrder.get(i).toLowerCase(), i);
        }

        // Sort sequence items by topological skill order
        List<GeminiCourseSequenceItemDto> sorted = new ArrayList<>(seq);
        sorted.sort(Comparator.comparingInt(item -> {
            CandidateCourseDto course = candidateMap.get(item.courseId());
            if (course == null || course.skillsCovered() == null || course.skillsCovered().isEmpty()) {
                return 999;
            }
            int minSkillRank = 999;
            for (String skill : course.skillsCovered()) {
                Integer rank = skillRankMap.get(skill.toLowerCase());
                if (rank != null && rank < minSkillRank) {
                    minSkillRank = rank;
                }
            }
            return minSkillRank;
        }));

        // Reassign 1-indexed order numbers
        List<GeminiCourseSequenceItemDto> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            GeminiCourseSequenceItemDto orig = sorted.get(i);
            result.add(new GeminiCourseSequenceItemDto(orig.courseId(), i + 1, orig.reason()));
        }
        return result;
    }
}
