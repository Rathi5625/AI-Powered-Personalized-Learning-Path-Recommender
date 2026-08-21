package com.learningpath.service;

import com.learningpath.dto.NotificationDto;
import com.learningpath.entity.Notification;
import com.learningpath.entity.enums.NotificationCategory;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(UUID userId, NotificationCategory category) {
        List<Notification> notifications;
        if (category != null) {
            notifications = notificationRepository.findAllByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
        } else {
            notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }

        return notifications.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this notification");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToDto(saved);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
        log.info("[NotificationService] Marked all notifications as read for userId={}", userId);
    }

    @Transactional
    public Notification createNotification(UUID userId, String title, String message, NotificationCategory category, String actionUrl) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .category(category != null ? category : NotificationCategory.SYSTEM)
                .read(false)
                .actionUrl(actionUrl)
                .build();

        return notificationRepository.save(notification);
    }

    private NotificationDto mapToDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getCategory(),
                n.isRead(),
                n.getActionUrl(),
                n.getCreatedAt()
        );
    }
}
