package com.learningpath.controller;

import com.learningpath.dto.CreateSupportTicketRequest;
import com.learningpath.dto.SupportTicketDto;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    public ResponseEntity<List<SupportTicketDto>> getTickets(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(supportTicketService.getTicketsForUser(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketDto> getTicketById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(supportTicketService.getTicketById(id, principal.getId()));
    }

    @PostMapping
    public ResponseEntity<SupportTicketDto> createTicket(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateSupportTicketRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        SupportTicketDto response = supportTicketService.createTicket(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
