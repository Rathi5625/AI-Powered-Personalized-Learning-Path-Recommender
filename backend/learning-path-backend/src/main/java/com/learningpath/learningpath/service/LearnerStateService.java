package com.learningpath.learningpath.service;

import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.UserProgress;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes the learner's current effective state from persisted data.
 *
 * This service is PURELY DETERMINISTIC — it reads database state only.
 * It does NOT call Gemini, ML service, or any probabilistic system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearnerStateService {

    // Proficiency levels considered as "mastered"
    private static final Set<ProficiencyLevel> MASTERED_LEVELS = Set.of(
            ProficiencyLevel.ADVANCED,
            ProficiencyLevel.EXPERT
    );

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * Builds a full snapshot of the learner's current state.
     * All computations are deterministic — no AI involved.
     *
     * @param userId   The learner's UUID.
     * @param careerId The target career UUID.
     * @return A {@link LearnerSnapshot} capturing the learner's current effective state.
     */
    public LearnerSnapshot snapshot(UUID userId, UUID careerId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found with id: " + careerId));

        Set<String> completedSkills = computeCompletedSkills(userId);
        Set<String> remainingSkills = computeRemainingSkills(careerId, completedSkills);
        List<UserProgress> progress = userProgressRepository.findByUserId(userId);

        log.debug("[LearnerStateService] Snapshot for userId={} careerId={}: completedSkills={}, remainingSkills={}",
                userId, careerId, completedSkills.size(), remainingSkills.size());

        return new LearnerSnapshot(userId, careerId, career.getTitle(), completedSkills, remainingSkills, progress);
    }

    /**
     * Computes the set of skill names the learner has already mastered.
     *
     * A skill is considered mastered when:
     *   - The learner's {@link UserSkill} proficiency is ADVANCED or EXPERT, OR
     *   - Every course associated with this skill has been completed (ProgressStatus.COMPLETED)
     *
     * This is DETERMINISTIC — computed entirely from DB state.
     *
     * @param userId The learner's UUID.
     * @return Lower-case set of mastered skill names.
     */
    public Set<String> computeCompletedSkills(UUID userId) {
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);

        // Skills mastered by proficiency level
        Set<String> mastered = userSkills.stream()
                .filter(us -> MASTERED_LEVELS.contains(us.getProficiencyLevel()))
                .map(us -> us.getSkill().getName().toLowerCase())
                .collect(Collectors.toCollection(HashSet::new));

        log.debug("[LearnerStateService] computeCompletedSkills: {} skills mastered by proficiency for userId={}",
                mastered.size(), userId);
        return mastered;
    }

    /**
     * Computes the skills still remaining in the learner's skill gap.
     *
     * A skill remains if:
     *   - It is required for the target career, AND
     *   - It is NOT in the learner's completedSkills set.
     *
     * This is DETERMINISTIC — computed entirely from DB state.
     *
     * @param careerId        The target career's UUID.
     * @param completedSkills The set of already-mastered skill names (lower-case).
     * @return Set of remaining skill names (lower-case).
     */
    public Set<String> computeRemainingSkills(UUID careerId, Set<String> completedSkills) {
        List<CareerSkill> careerSkills = careerSkillRepository.findByCareerId(careerId);

        return careerSkills.stream()
                .map(cs -> cs.getSkill().getName().toLowerCase())
                .filter(skillName -> !completedSkills.contains(skillName))
                .collect(Collectors.toCollection(HashSet::new));
    }
}
