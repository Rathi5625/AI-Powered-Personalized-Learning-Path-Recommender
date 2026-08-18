package com.learningpath.recommendation.service;

import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.engine.SkillGapEngine;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillGapService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final UserSkillRepository userSkillRepository;
    private final CareerSkillRepository careerSkillRepository;

    private final SkillGapEngine engine = new SkillGapEngine();

    public SkillGapAnalysisResponse analyzeSkillGap(UUID userId, UUID careerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Career career;
        if (careerId != null) {
            career = careerRepository.findById(careerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Career not found with id: " + careerId));
        } else {
            String targetCareerTitle = user.getTargetCareer();
            if (targetCareerTitle == null || targetCareerTitle.trim().isEmpty()) {
                throw new ResourceNotFoundException("User does not have a target career set. Please specify a careerId or set a target career.");
            }
            career = careerRepository.findByTitle(targetCareerTitle.trim())
                    .orElseGet(() -> {
                        List<Career> matches = careerRepository.findByTitleContainingIgnoreCase(targetCareerTitle.trim());
                        if (matches.isEmpty()) {
                            throw new ResourceNotFoundException("Target career '" + targetCareerTitle + "' not found in careers catalog");
                        }
                        return matches.get(0);
                    });
        }

        UUID resolvedCareerId = career.getId();
        List<CareerSkill> careerSkills = careerSkillRepository.findByCareerId(resolvedCareerId);
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);

        Map<UUID, UserSkill> userSkillMap = userSkills.stream()
                .collect(Collectors.toMap(us -> us.getSkill().getId(), Function.identity(), (existing, replacement) -> existing));

        List<SkillGapItemResponse> gapItems = new ArrayList<>();
        int noGapCount = 0;
        int partialGapCount = 0;
        int fullGapCount = 0;

        for (CareerSkill cs : careerSkills) {
            UserSkill us = userSkillMap.get(cs.getSkill().getId());
            SkillGapItemResponse item = engine.evaluateSkillGap(cs, us);
            gapItems.add(item);

            if (item.gapType() == GapType.NO_GAP) {
                noGapCount++;
            } else if (item.gapType() == GapType.PARTIAL_GAP) {
                partialGapCount++;
            } else {
                fullGapCount++;
            }
        }

        double overallGapScore = engine.calculateOverallGapScore(careerSkills, userSkillMap);

        return new SkillGapAnalysisResponse(
                user.getId(),
                user.getFullName(),
                career.getId(),
                career.getTitle(),
                careerSkills.size(),
                noGapCount,
                partialGapCount,
                fullGapCount,
                overallGapScore,
                gapItems
        );
    }
}
