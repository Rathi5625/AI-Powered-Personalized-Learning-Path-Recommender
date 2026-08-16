package com.learningpath.controller;

import com.learningpath.dto.CareerRequest;
import com.learningpath.dto.CareerResponse;
import com.learningpath.service.CareerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @PostMapping
    public ResponseEntity<CareerResponse> createCareer(@Valid @RequestBody CareerRequest request) {
        CareerResponse response = careerService.createCareer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CareerResponse>> getAllCareers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CareerResponse> careers = careerService.getAllCareers(pageable);
        return ResponseEntity.ok(careers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareerResponse> getCareerById(@PathVariable UUID id) {
        CareerResponse response = careerService.getCareerById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareerResponse> updateCareer(
            @PathVariable UUID id,
            @Valid @RequestBody CareerRequest request
    ) {
        CareerResponse response = careerService.updateCareer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCareer(@PathVariable UUID id) {
        careerService.deleteCareer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CareerResponse>> searchCareers(@RequestParam String name) {
        List<CareerResponse> careers = careerService.searchCareersByName(name);
        return ResponseEntity.ok(careers);
    }
}
