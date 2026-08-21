package com.learningpath.controller;

import com.learningpath.dto.UpdateProfileRequest;
import com.learningpath.dto.UserProfileResponse;
import com.learningpath.dto.UserSkillRequest;
import com.learningpath.dto.UserSkillResponse;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.ProfileService;
import com.learningpath.service.UserSkillService;
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
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserSkillService userSkillService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(profileService.getProfile(principal.getId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(profileService.updateProfile(principal.getId(), request));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<UserSkillResponse>> getProfileSkills(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return ResponseEntity.ok(userSkillService.getUserSkills(principal.getId()));
    }

    @PostMapping("/skills")
    public ResponseEntity<UserSkillResponse> addProfileSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserSkillRequest request
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        UserSkillResponse response = userSkillService.addUserSkill(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<Void> deleteProfileSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID skillId
    ) {
        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        userSkillService.removeUserSkill(principal.getId(), skillId);
        return ResponseEntity.noContent().build();
    }
}
