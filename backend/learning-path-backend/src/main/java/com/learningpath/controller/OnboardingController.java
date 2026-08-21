package com.learningpath.controller;

import com.learningpath.dto.OnboardingCompleteRequest;
import com.learningpath.dto.UserProfileResponse;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete")
    public ResponseEntity<UserProfileResponse> completeOnboarding(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OnboardingCompleteRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(onboardingService.completeOnboarding(principal.getId(), request));
    }
}
