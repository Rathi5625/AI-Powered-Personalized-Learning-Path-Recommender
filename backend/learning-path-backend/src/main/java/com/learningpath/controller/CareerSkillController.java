package com.learningpath.controller;

import com.learningpath.dto.CareerSkillRequest;
import com.learningpath.dto.CareerSkillResponse;
import com.learningpath.dto.CareerSkillUpdateRequest;
import com.learningpath.service.CareerSkillService;
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
@RequestMapping("/api/careers/{careerId}/skills")
@RequiredArgsConstructor
public class CareerSkillController {

    private final CareerSkillService careerSkillService;

    @PostMapping
    public ResponseEntity<CareerSkillResponse> addCareerSkill(
            @PathVariable UUID careerId,
            @Valid @RequestBody CareerSkillRequest request
    ) {
        CareerSkillResponse response = careerSkillService.addCareerSkill(careerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CareerSkillResponse>> getCareerSkills(@PathVariable UUID careerId) {
        List<CareerSkillResponse> skills = careerSkillService.getCareerSkills(careerId);
        return ResponseEntity.ok(skills);
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<CareerSkillResponse> updateCareerSkill(
            @PathVariable UUID careerId,
            @PathVariable UUID skillId,
            @Valid @RequestBody CareerSkillUpdateRequest request
    ) {
        CareerSkillResponse response = careerSkillService.updateCareerSkill(careerId, skillId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> removeCareerSkill(
            @PathVariable UUID careerId,
            @PathVariable UUID skillId
    ) {
        careerSkillService.removeCareerSkill(careerId, skillId);
        return ResponseEntity.noContent().build();
    }
}
