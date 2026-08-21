package com.learningpath.adaptive.service;

import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.MasteryLevel;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerMasteryService {

    private final LearnerKnowledgeStateRepository knowledgeStateRepository;

    @Transactional(readOnly = true)
    public LearnerMasteryDto.Summary getMasterySummary(UUID userId) {
        List<LearnerKnowledgeState> states = knowledgeStateRepository.findByUserId(userId);

        if (states.isEmpty()) {
            return LearnerMasteryDto.Summary.builder()
                    .totalConceptsTracked(0)
                    .overallMasteryPercentage(0.0)
                    .masteredSkills(List.of())
                    .developingSkills(List.of())
                    .weakSkills(List.of())
                    .revisionRequiredSkills(List.of())
                    .conceptStates(List.of())
                    .build();
        }

        List<String> mastered = new ArrayList<>();
        List<String> developing = new ArrayList<>();
        List<String> weak = new ArrayList<>();
        List<String> revision = new ArrayList<>();

        double sumProb = 0.0;

        List<LearnerMasteryDto.ConceptItem> items = new ArrayList<>();

        for (LearnerKnowledgeState s : states) {
            sumProb += s.getKnowledgeProbability();

            if (s.getMasteryLevel() == MasteryLevel.MASTERED || s.getMasteryLevel() == MasteryLevel.PROFICIENT) {
                mastered.add(s.getConceptName());
            } else if (s.getMasteryLevel() == MasteryLevel.DEVELOPING || s.getMasteryLevel() == MasteryLevel.BASIC) {
                developing.add(s.getConceptName());
            } else {
                weak.add(s.getConceptName());
            }

            if (s.isRevisionRequired()) {
                revision.add(s.getConceptName());
            }

            items.add(LearnerMasteryDto.ConceptItem.builder()
                    .id(s.getId().toString())
                    .conceptName(s.getConceptName())
                    .skillId(s.getSkill() != null ? s.getSkill().getId().toString() : null)
                    .knowledgeProbability(s.getKnowledgeProbability())
                    .masteryLevel(s.getMasteryLevel())
                    .attempts(s.getAttempts())
                    .correctAttempts(s.getCorrectAttempts())
                    .confidenceScore(s.getConfidenceScore())
                    .revisionRequired(s.isRevisionRequired())
                    .lastAttemptAt(s.getLastAttemptAt() != null ? s.getLastAttemptAt().toString() : null)
                    .build());
        }

        double overallPct = Math.round((sumProb / states.size()) * 1000.0) / 10.0;

        return LearnerMasteryDto.Summary.builder()
                .totalConceptsTracked(states.size())
                .overallMasteryPercentage(overallPct)
                .masteredSkills(mastered)
                .developingSkills(developing)
                .weakSkills(weak)
                .revisionRequiredSkills(revision)
                .conceptStates(items)
                .build();
    }

    @Transactional(readOnly = true)
    public List<LearnerMasteryDto.ConceptItem> getWeakSkills(UUID userId) {
        return knowledgeStateRepository.findWeakestConceptsByUserId(userId).stream()
                .filter(s -> s.getKnowledgeProbability() < 0.50)
                .map(s -> LearnerMasteryDto.ConceptItem.builder()
                        .id(s.getId().toString())
                        .conceptName(s.getConceptName())
                        .skillId(s.getSkill() != null ? s.getSkill().getId().toString() : null)
                        .knowledgeProbability(s.getKnowledgeProbability())
                        .masteryLevel(s.getMasteryLevel())
                        .attempts(s.getAttempts())
                        .correctAttempts(s.getCorrectAttempts())
                        .confidenceScore(s.getConfidenceScore())
                        .revisionRequired(s.isRevisionRequired())
                        .lastAttemptAt(s.getLastAttemptAt() != null ? s.getLastAttemptAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LearnerMasteryDto.ConceptItem> getRevisionRequired(UUID userId) {
        return knowledgeStateRepository.findByUserIdAndRevisionRequiredTrue(userId).stream()
                .map(s -> LearnerMasteryDto.ConceptItem.builder()
                        .id(s.getId().toString())
                        .conceptName(s.getConceptName())
                        .skillId(s.getSkill() != null ? s.getSkill().getId().toString() : null)
                        .knowledgeProbability(s.getKnowledgeProbability())
                        .masteryLevel(s.getMasteryLevel())
                        .attempts(s.getAttempts())
                        .correctAttempts(s.getCorrectAttempts())
                        .confidenceScore(s.getConfidenceScore())
                        .revisionRequired(s.isRevisionRequired())
                        .lastAttemptAt(s.getLastAttemptAt() != null ? s.getLastAttemptAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
