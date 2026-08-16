package com.learningpath.service;

import com.learningpath.dto.CareerSkillRequest;
import com.learningpath.dto.CareerSkillResponse;
import com.learningpath.dto.CareerSkillUpdateRequest;
import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.Skill;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerSkillService {

    private final CareerSkillRepository careerSkillRepository;
    private final CareerRepository careerRepository;
    private final SkillRepository skillRepository;

    public CareerSkillResponse addCareerSkill(UUID careerId, CareerSkillRequest request) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found with id: " + careerId));

        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.skillId()));

        if (careerSkillRepository.existsByCareerIdAndSkillId(careerId, request.skillId())) {
            throw new DuplicateResourceException("Career already has required skill '" + skill.getName() + "' assigned");
        }

        boolean isMandatory = request.isMandatory() != null ? request.isMandatory() : true;

        CareerSkill careerSkill = CareerSkill.builder()
                .career(career)
                .skill(skill)
                .priority(request.priority())
                .requiredProficiency(request.requiredProficiency())
                .isMandatory(isMandatory)
                .build();

        CareerSkill savedCareerSkill = careerSkillRepository.save(careerSkill);
        return mapToCareerSkillResponse(savedCareerSkill);
    }

    @Transactional(readOnly = true)
    public List<CareerSkillResponse> getCareerSkills(UUID careerId) {
        if (!careerRepository.existsById(careerId)) {
            throw new ResourceNotFoundException("Career not found with id: " + careerId);
        }

        return careerSkillRepository.findByCareerId(careerId)
                .stream()
                .map(this::mapToCareerSkillResponse)
                .toList();
    }

    public CareerSkillResponse updateCareerSkill(UUID careerId, UUID skillId, CareerSkillUpdateRequest request) {
        if (!careerRepository.existsById(careerId)) {
            throw new ResourceNotFoundException("Career not found with id: " + careerId);
        }

        CareerSkill careerSkill = careerSkillRepository.findByCareerIdAndSkillId(careerId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found for career " + careerId));

        if (request.priority() != null) {
            careerSkill.setPriority(request.priority());
        }
        if (request.requiredProficiency() != null) {
            careerSkill.setRequiredProficiency(request.requiredProficiency());
        }
        if (request.isMandatory() != null) {
            careerSkill.setMandatory(request.isMandatory());
        }

        CareerSkill updatedCareerSkill = careerSkillRepository.save(careerSkill);
        return mapToCareerSkillResponse(updatedCareerSkill);
    }

    public void removeCareerSkill(UUID careerId, UUID skillId) {
        if (!careerRepository.existsById(careerId)) {
            throw new ResourceNotFoundException("Career not found with id: " + careerId);
        }

        CareerSkill careerSkill = careerSkillRepository.findByCareerIdAndSkillId(careerId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found for career " + careerId));

        careerSkillRepository.delete(careerSkill);
    }

    private CareerSkillResponse mapToCareerSkillResponse(CareerSkill careerSkill) {
        return new CareerSkillResponse(
                careerSkill.getId(),
                careerSkill.getCareer().getId(),
                careerSkill.getCareer().getTitle(),
                careerSkill.getSkill().getId(),
                careerSkill.getSkill().getName(),
                careerSkill.getSkill().getCategory(),
                careerSkill.getPriority(),
                careerSkill.getRequiredProficiency(),
                careerSkill.isMandatory(),
                careerSkill.getCreatedAt(),
                careerSkill.getUpdatedAt()
        );
    }
}
