package com.learningpath.service;

import com.learningpath.entity.LearningActivity;
import com.learningpath.entity.enums.ActivityType;
import com.learningpath.repository.LearningActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningActivityService {

    private final LearningActivityRepository learningActivityRepository;

    @Transactional
    public void logActivity(UUID userId, ActivityType activityType, String entityType, String entityId, String metadata, Integer durationSeconds) {
        try {
            LearningActivity activity = LearningActivity.builder()
                    .userId(userId)
                    .activityType(activityType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadata)
                    .durationSeconds(durationSeconds)
                    .build();

            learningActivityRepository.save(activity);
            log.debug("[LearningActivityService] Logged activity: type={}, user={}", activityType, userId);
        } catch (Exception ex) {
            log.warn("[LearningActivityService] Failed to log activity: {}", ex.getMessage());
        }
    }
}
