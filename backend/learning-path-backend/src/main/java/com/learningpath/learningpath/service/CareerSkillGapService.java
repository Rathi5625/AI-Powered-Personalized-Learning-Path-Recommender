package com.learningpath.learningpath.service;

import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.User;
import com.learningpath.entity.UserSkill;
import com.learningpath.learningpath.dto.SkillGapDetailDto;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CareerSkillGapService {

    private final CareerRepository careerRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final UserSkillRepository userSkillRepository;
    private final LearnerKnowledgeStateRepository knowledgeStateRepository;
    private final UserRepository userRepository;

    private static final Map<String, List<String>> DEFAULT_CAREER_SKILLS = Map.of(
            "Software Engineer", List.of("Java", "OOP", "Data Structures", "Algorithms", "Binary Search", "Trees", "SQL", "Git", "Testing"),
            "Full Stack Developer", List.of("Java", "Spring Boot", "REST APIs", "SQL", "React", "JavaScript", "HTML", "CSS", "Git"),
            "Backend Developer", List.of("Java", "Spring Boot", "Microservices", "REST APIs", "SQL", "Database Design", "Docker", "Git"),
            "Frontend Developer", List.of("JavaScript", "TypeScript", "React", "HTML", "CSS", "Responsive Design", "Git", "Testing"),
            "Data Scientist", List.of("Python", "SQL", "Pandas", "Machine Learning", "Data Analysis", "Statistics", "Git")
    );

    @Transactional(readOnly = true)
    public List<SkillGapDetailDto> analyzeGaps(UUID userId, UUID careerId) {
        log.info("[CareerSkillGapService] Analyzing real skill gaps for userId={}, careerId={}", userId, careerId);

        User user = userRepository.findById(userId).orElse(null);
        String targetCareerTitle = null;
        if (careerId != null) {
            targetCareerTitle = careerRepository.findById(careerId).map(Career::getTitle).orElse(null);
        }
        if (targetCareerTitle == null && user != null) {
            targetCareerTitle = user.getTargetCareer();
        }
        if (targetCareerTitle == null || targetCareerTitle.isBlank()) {
            targetCareerTitle = "Software Engineer";
        }

        // 1. Gather required skills for this career
        Set<String> requiredSkillNames = new LinkedHashSet<>();
        if (careerId != null) {
            List<CareerSkill> careerSkills = careerSkillRepository.findByCareerId(careerId);
            for (CareerSkill cs : careerSkills) {
                if (cs.getSkill() != null) {
                    requiredSkillNames.add(cs.getSkill().getName());
                }
            }
        }
        if (requiredSkillNames.isEmpty()) {
            // Check matching career in map or partial match
            List<String> defaults = DEFAULT_CAREER_SKILLS.get(targetCareerTitle);
            if (defaults == null) {
                for (Map.Entry<String, List<String>> entry : DEFAULT_CAREER_SKILLS.entrySet()) {
                    if (targetCareerTitle.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        defaults = entry.getValue();
                        break;
                    }
                }
            }
            if (defaults == null) {
                defaults = DEFAULT_CAREER_SKILLS.get("Software Engineer");
            }
            requiredSkillNames.addAll(defaults);
        }

        // 2. Load learner's verified user skills
        Map<String, Double> userSkillProficiencies = new HashMap<>();
        List<UserSkill> verifiedSkills = userSkillRepository.findByUserId(userId);
        for (UserSkill us : verifiedSkills) {
            if (us.getSkill() != null) {
                double level = 0.40;
                if (us.getProficiencyLevel() != null) {
                    switch (us.getProficiencyLevel()) {
                        case BEGINNER -> level = 0.35;
                        case INTERMEDIATE -> level = 0.65;
                        case ADVANCED -> level = 0.85;
                        case EXPERT -> level = 0.95;
                    }
                }
                userSkillProficiencies.put(us.getSkill().getName().toLowerCase(), level);
            }
        }


        // 3. Load learner's BKT Knowledge States
        Map<String, LearnerKnowledgeState> bktStates = knowledgeStateRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        k -> k.getConceptName().toLowerCase(),
                        k -> k,
                        (k1, k2) -> k1.getUpdatedAt().isAfter(k2.getUpdatedAt()) ? k1 : k2
                ));

        // 4. Calculate exact gaps
        List<SkillGapDetailDto> results = new ArrayList<>();
        int index = 0;
        for (String skillName : requiredSkillNames) {
            String lower = skillName.toLowerCase();
            LearnerKnowledgeState bkt = bktStates.get(lower);

            double currentMastery = 0.0;
            boolean revisionRequired = false;

            if (bkt != null) {
                currentMastery = bkt.getKnowledgeProbability();
                revisionRequired = bkt.isRevisionRequired();
            } else if (userSkillProficiencies.containsKey(lower)) {
                currentMastery = userSkillProficiencies.get(lower);
            }

            double requiredLevel = (index < 4) ? 0.80 : 0.70; // Core fundamentals require higher threshold
            double gap = Math.max(0.0, requiredLevel - currentMastery);
            double priority = Math.round((gap * (1.0 - (index * 0.05))) * 100.0) / 100.0;

            String status = "NOT_STARTED";
            if (revisionRequired) {
                status = "REVISION_REQUIRED";
            } else if (currentMastery >= 0.85) {
                status = "MASTERED";
            } else if (currentMastery >= 0.70) {
                status = "PROFICIENT";
            } else if (currentMastery >= 0.30) {
                status = "DEVELOPING";
            }

            results.add(SkillGapDetailDto.builder()
                    .skill(skillName)
                    .requiredLevel(requiredLevel)
                    .currentMastery(Math.round(currentMastery * 100.0) / 100.0)
                    .gap(Math.round(gap * 100.0) / 100.0)
                    .priority(Math.max(0.1, priority))
                    .status(status)
                    .build());
            index++;
        }

        // Sort by priority descending (highest gap and highest relevance first)
        results.sort((a, b) -> Double.compare(b.getPriority(), a.getPriority()));
        return results;
    }
}
