package com.learningpath.controller;

import com.learningpath.dto.AdminTestResponse;
import com.learningpath.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTestResponse> getAdminStatus(@AuthenticationPrincipal UserPrincipal principal) {
        String email = principal != null ? principal.getEmail() : "admin@learnai.local";
        log.info("[AdminController] Admin verified access for email={}", email);
        return ResponseEntity.ok(new AdminTestResponse(
                "ADMIN",
                "Admin authentication successful",
                email
        ));
    }
}
