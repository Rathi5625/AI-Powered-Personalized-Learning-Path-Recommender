package com.learningpath.learningpath.service;

import com.learningpath.entity.LearningPath;
import com.learningpath.entity.LearningPathItem;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.learningpath.dto.LearningPathNodeDto;
import com.learningpath.learningpath.dto.WeeklyDayScheduleDto;
import com.learningpath.learningpath.dto.WeeklyLearningPlanDto;
import com.learningpath.repository.LearningPathItemRepository;
import com.learningpath.repository.LearningPathRepository;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyLearningPlanService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathItemRepository itemRepository;
    private final UserRepository userRepository;
    private final LearningPathEngineService engineService;

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Transactional(readOnly = true)
    public WeeklyLearningPlanDto getWeeklyPlan(UUID userId) {
        log.info("[WeeklyLearningPlanService] Generating weekly schedule for userId={}", userId);

        User user = userRepository.findById(userId).orElse(null);
        int dailyHours = (user != null && user.getDailyLearningHours() != null) ? user.getDailyLearningHours() : 2;
        int weeklyTargetMinutes = dailyHours * 5 * 60; // 5 active days target

        LearningPath path = learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE).orElse(null);
        if (path == null) {
            // Generate path if not found
            UUID careerId = (path != null && path.getTargetCareer() != null) ? path.getTargetCareer().getId() : null;
            engineService.generatePath(userId, careerId, "Weekly plan initialization");
            path = learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE).orElse(null);
        }

        List<LearningPathItem> items = (path != null)
                ? itemRepository.findByLearningPathIdOrderByItemOrderAsc(path.getId())
                : Collections.emptyList();

        // Filter active/actionable items (in_progress, revision, unlocked, incomplete)
        List<LearningPathItem> actionableItems = items.stream()
                .filter(i -> !i.isCompleted() && i.getStatus() != LearningPathNodeStatus.LOCKED)
                .toList();

        if (actionableItems.isEmpty()) {
            actionableItems = items.stream().filter(i -> !i.isCompleted()).toList();
        }

        List<WeeklyDayScheduleDto> daySchedules = new ArrayList<>();
        int scheduledTotalMinutes = 0;
        int itemIndex = 0;

        String focusTopic = !actionableItems.isEmpty() && actionableItems.get(0).getTargetSkill() != null
                ? actionableItems.get(0).getTargetSkill().getName()
                : "Foundational Programming & DSA";

        for (int d = 0; d < 5; d++) {
            String dayName = DAYS[d];
            int dailyBudget = dailyHours * 60;
            int dayAllocated = 0;
            List<LearningPathNodeDto> dayNodes = new ArrayList<>();

            while (itemIndex < actionableItems.size() && (dayAllocated + actionableItems.get(itemIndex).getEstimatedMinutes() <= dailyBudget + 15 || dayNodes.isEmpty())) {
                LearningPathItem item = actionableItems.get(itemIndex);
                int duration = item.getEstimatedMinutes() != null ? item.getEstimatedMinutes() : 45;

                dayNodes.add(LearningPathNodeDto.builder()
                        .id(item.getId())
                        .nodeType(item.getNodeType())
                        .title(item.getTitle())
                        .description(item.getExplanation())
                        .skillName(item.getTargetSkill() != null ? item.getTargetSkill().getName() : null)
                        .status(item.getStatus())
                        .difficulty(item.getDifficulty())
                        .estimatedMinutes(duration)
                        .actionUrl(item.getActionUrl())
                        .reason(item.getExplanation())
                        .completed(item.isCompleted())
                        .build());

                dayAllocated += duration;
                itemIndex++;
                if (itemIndex >= actionableItems.size()) {
                    break;
                }
            }

            daySchedules.add(WeeklyDayScheduleDto.builder()
                    .dayName(dayName)
                    .dayIndex(d + 1)
                    .allocatedMinutes(dayAllocated)
                    .activities(dayNodes)
                    .build());

            scheduledTotalMinutes += dayAllocated;
        }

        return WeeklyLearningPlanDto.builder()
                .weekNumber(1)
                .weeklyTargetMinutes(weeklyTargetMinutes)
                .scheduledMinutes(scheduledTotalMinutes)
                .focusTopic(focusTopic)
                .days(daySchedules)
                .build();
    }
}
