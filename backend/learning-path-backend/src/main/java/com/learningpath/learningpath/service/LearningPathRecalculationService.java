package com.learningpath.learningpath.service;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathNodeType;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.entity.enums.NotificationCategory;
import com.learningpath.learningpath.dto.LearningPathFullResponse;
import com.learningpath.repository.*;
import com.learningpath.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathRecalculationService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathItemRepository itemRepository;
    private final LearnerKnowledgeStateRepository knowledgeStateRepository;
    private final LearningPathVersionRepository versionRepository;
    private final LearningPathEngineService engineService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public LearningPathFullResponse triggerRecalculation(UUID userId, String triggerReason) {
        log.info("[LearningPathRecalculationService] Recalculating path for userId={}, triggerReason={}", userId, triggerReason);

        LearningPath activePath = learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE).orElse(null);
        if (activePath == null) {
            log.info("[LearningPathRecalculationService] No active path found for userId={}, generating new path.", userId);
            User user = userRepository.findById(userId).orElse(null);
            UUID careerId = (user != null && user.getTargetCareer() != null) ? null : null;
            return engineService.generatePath(userId, careerId, triggerReason);
        }

        List<LearningPathItem> items = itemRepository.findByLearningPathIdOrderByItemOrderAsc(activePath.getId());
        List<LearnerKnowledgeState> knowledgeStates = knowledgeStateRepository.findByUserId(userId);
        Map<String, LearnerKnowledgeState> knowledgeMap = new HashMap<>();
        for (LearnerKnowledgeState ks : knowledgeStates) {
            knowledgeMap.put(ks.getConceptName().toLowerCase(), ks);
        }

        boolean stateChanged = false;
        List<String> unlockedNodes = new ArrayList<>();
        List<String> revisedNodes = new ArrayList<>();

        LearningPathItem previousCourseNode = null;

        for (LearningPathItem item : items) {
            String skillName = item.getTargetSkill() != null ? item.getTargetSkill().getName() : item.getTitle();
            LearnerKnowledgeState state = skillName != null ? knowledgeMap.get(skillName.toLowerCase()) : null;

            double currentMastery = state != null ? state.getKnowledgeProbability() : 0.0;
            item.setCurrentMastery(currentMastery);

            // 1. Mastery Completion Check
            if (currentMastery >= 0.85 && !item.isCompleted()) {
                item.setCompleted(true);
                item.setStatus(LearningPathNodeStatus.COMPLETED);
                item.setCompletedAt(Instant.now());
                stateChanged = true;
            }

            // 2. Revision Required Check
            if (state != null && state.isRevisionRequired() && item.getStatus() != LearningPathNodeStatus.REVISION_REQUIRED) {
                item.setStatus(LearningPathNodeStatus.REVISION_REQUIRED);
                revisedNodes.add(skillName);
                stateChanged = true;
            }

            // 3. Prerequisite Gate Unlocking
            if (item.getStatus() == LearningPathNodeStatus.LOCKED && previousCourseNode != null) {
                if (previousCourseNode.getCurrentMastery() >= 0.65 || previousCourseNode.isCompleted()) {
                    item.setStatus(LearningPathNodeStatus.UNLOCKED);
                    item.setUnlockReason("Unlocked: Prerequisites in " + previousCourseNode.getTitle() + " completed with sufficient mastery.");
                    unlockedNodes.add(item.getTitle());
                    stateChanged = true;
                }
            }

            if (item.getNodeType() == LearningPathNodeType.COURSE) {
                previousCourseNode = item;
            }
        }

        if (stateChanged) {
            itemRepository.saveAll(items);

            int currentVer = (activePath.getVersion() != null ? activePath.getVersion() : 1) + 1;
            activePath.setVersion(currentVer);
            activePath.setLastRecalculatedAt(Instant.now());
            activePath.setRecalculationReason(triggerReason);

            // Recalculate progress
            long completedCount = items.stream().filter(LearningPathItem::isCompleted).count();
            double progress = items.isEmpty() ? 0.0 : Math.round(((double) completedCount / items.size()) * 1000.0) / 10.0;
            activePath.setOverallProgress(progress);
            learningPathRepository.save(activePath);

            // Explain change
            StringBuilder explanation = new StringBuilder();
            if (!unlockedNodes.isEmpty()) {
                explanation.append("New modules unlocked: ").append(String.join(", ", unlockedNodes)).append(". ");
            }
            if (!revisedNodes.isEmpty()) {
                explanation.append("Targeted revision added for: ").append(String.join(", ", revisedNodes)).append(". ");
            }
            if (explanation.length() == 0) {
                explanation.append("Adaptive learning path calibrated based on your recent activity: ").append(triggerReason);
            }

            // Record Version Audit
            LearningPathVersion version = LearningPathVersion.builder()
                    .learningPath(activePath)
                    .user(activePath.getUser())
                    .versionNumber(currentVer)
                    .changeReason(triggerReason)
                    .explanation(explanation.toString())
                    .overallProgress(progress)
                    .build();
            versionRepository.save(version);

            // Fire Notification
            try {
                notificationService.createNotification(
                        userId,
                        "Learning Path Updated (v" + currentVer + ")",
                        explanation.toString(),
                        NotificationCategory.LEARNING,
                        "/learning-path"
                );
            } catch (Exception e) {
                log.warn("[LearningPathRecalculationService] Notification error: {}", e.getMessage());
            }


            log.info("[LearningPathRecalculationService] Path successfully updated to v{} for userId={}", currentVer, userId);
        }

        // Return full fresh DTO
        User user = activePath.getUser();
        UUID careerId = activePath.getTargetCareer() != null ? activePath.getTargetCareer().getId() : null;

        if (stateChanged) {
            // Re-generate path view to reflect updated items
            return engineService.generatePath(userId, careerId, triggerReason);
        } else {
            log.info("[LearningPathRecalculationService] No state changes detected for userId={}, returning existing path view.", userId);
            return engineService.generatePath(userId, careerId, "no_change_view");
        }
    }
}
