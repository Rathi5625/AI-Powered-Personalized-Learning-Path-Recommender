package com.learningpath.service;

import com.learningpath.dto.CreateSupportTicketRequest;
import com.learningpath.dto.SupportTicketDto;
import com.learningpath.entity.SupportTicket;
import com.learningpath.entity.enums.TicketPriority;
import com.learningpath.entity.enums.TicketStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Transactional(readOnly = true)
    public List<SupportTicketDto> getTicketsForUser(UUID userId) {
        return supportTicketRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupportTicketDto getTicketById(UUID ticketId, UUID userId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found"));

        if (!ticket.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to access this ticket");
        }

        return mapToDto(ticket);
    }

    @Transactional
    public SupportTicketDto createTicket(UUID userId, CreateSupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .userId(userId)
                .category(request.category().trim())
                .subject(request.subject().trim())
                .description(request.description().trim())
                .status(TicketStatus.OPEN)
                .priority(request.priority() != null ? request.priority() : TicketPriority.MEDIUM)
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);
        log.info("[SupportTicketService] Created ticket id={} for userId={}", saved.getId(), userId);

        return mapToDto(saved);
    }

    private SupportTicketDto mapToDto(SupportTicket t) {
        return new SupportTicketDto(
                t.getId(),
                t.getCategory(),
                t.getSubject(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
