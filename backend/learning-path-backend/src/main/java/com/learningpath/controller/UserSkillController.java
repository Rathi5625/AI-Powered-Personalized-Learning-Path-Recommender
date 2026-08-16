package com.learningpath.controller;

import com.learningpath.dto.UserSkillRequest;
import com.learningpath.dto.UserSkillResponse;
import com.learningpath.dto.UserSkillUpdateRequest;
import com.learningpath.service.UserSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/skills")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;

    @PostMapping
    public ResponseEntity<UserSkillResponse> addUserSkill(
            @PathVariable UUID userId,
            @Valid @RequestBody UserSkillRequest request
    ) {
        UserSkillResponse response = userSkillService.addUserSkill(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserSkillResponse>> getUserSkills(@PathVariable UUID userId) {
        List<UserSkillResponse> skills = userSkillService.getUserSkills(userId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{skillId}")
    public ResponseEntity<UserSkillResponse> getUserSkill(
            @PathVariable UUID userId,
            @PathVariable UUID skillId
    ) {
        UserSkillResponse response = userSkillService.getUserSkill(userId, skillId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<UserSkillResponse> updateUserSkill(
            @PathVariable UUID userId,
            @PathVariable UUID skillId,
            @Valid @RequestBody UserSkillUpdateRequest request
    ) {
        UserSkillResponse response = userSkillService.updateUserSkill(userId, skillId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> removeUserSkill(
            @PathVariable UUID userId,
            @PathVariable UUID skillId
    ) {
        userSkillService.removeUserSkill(userId, skillId);
        return ResponseEntity.noContent().build();
    }
}
