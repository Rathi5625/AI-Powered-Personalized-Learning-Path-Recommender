package com.learningpath.dto;

import com.learningpath.entity.enums.TicketPriority;
import com.learningpath.entity.enums.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketDto(
        UUID id,
        String category,
        String subject,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt,
        Instant updatedAt
) {
}
