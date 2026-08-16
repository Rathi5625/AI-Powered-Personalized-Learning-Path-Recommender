package com.learningpath.service;

import com.learningpath.dto.CareerRequest;
import com.learningpath.dto.CareerResponse;
import com.learningpath.entity.Career;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerService {

    private final CareerRepository careerRepository;

    public CareerResponse createCareer(CareerRequest request) {
        if (careerRepository.existsByTitle(request.name())) {
            throw new DuplicateResourceException("Career with name '" + request.name() + "' already exists");
        }

        Career career = Career.builder()
                .title(request.name())
                .description(request.description())
                .category(request.category())
                .industry(request.category())
                .build();

        Career savedCareer = careerRepository.save(career);
        return mapToCareerResponse(savedCareer);
    }

    @Transactional(readOnly = true)
    public Page<CareerResponse> getAllCareers(Pageable pageable) {
        return careerRepository.findAll(pageable)
                .map(this::mapToCareerResponse);
    }

    @Transactional(readOnly = true)
    public CareerResponse getCareerById(UUID id) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found with id: " + id));
        return mapToCareerResponse(career);
    }

    public CareerResponse updateCareer(UUID id, CareerRequest request) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found with id: " + id));

        if (careerRepository.existsByTitleAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Career with name '" + request.name() + "' already exists");
        }

        career.setTitle(request.name());
        career.setDescription(request.description());
        career.setCategory(request.category());
        career.setIndustry(request.category());

        Career updatedCareer = careerRepository.save(career);
        return mapToCareerResponse(updatedCareer);
    }

    public void deleteCareer(UUID id) {
        if (!careerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Career not found with id: " + id);
        }
        careerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CareerResponse> searchCareersByName(String name) {
        return careerRepository.findByTitleContainingIgnoreCase(name)
                .stream()
                .map(this::mapToCareerResponse)
                .toList();
    }

    private CareerResponse mapToCareerResponse(Career career) {
        return new CareerResponse(
                career.getId(),
                career.getTitle(),
                career.getDescription(),
                career.getCategory() != null ? career.getCategory() : career.getIndustry(),
                career.getCreatedAt(),
                career.getUpdatedAt()
        );
    }
}
