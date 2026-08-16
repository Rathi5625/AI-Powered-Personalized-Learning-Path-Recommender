package com.learningpath.service;

import com.learningpath.dto.SkillRequest;
import com.learningpath.dto.SkillResponse;
import com.learningpath.entity.Skill;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.SkillRepository;
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
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Skill with name '" + request.name() + "' already exists");
        }

        Skill skill = Skill.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .difficulty(request.difficulty())
                .build();

        Skill savedSkill = skillRepository.save(skill);
        return mapToSkillResponse(savedSkill);
    }

    @Transactional(readOnly = true)
    public Page<SkillResponse> getAllSkills(Pageable pageable) {
        return skillRepository.findAll(pageable)
                .map(this::mapToSkillResponse);
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        return mapToSkillResponse(skill);
    }

    public SkillResponse updateSkill(UUID id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        if (skillRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Skill with name '" + request.name() + "' already exists");
        }

        skill.setName(request.name());
        skill.setCategory(request.category());
        skill.setDescription(request.description());
        skill.setDifficulty(request.difficulty());

        Skill updatedSkill = skillRepository.save(skill);
        return mapToSkillResponse(updatedSkill);
    }

    public void deleteSkill(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found with id: " + id);
        }
        skillRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkillsByName(String name) {
        return skillRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToSkillResponse)
                .toList();
    }

    private SkillResponse mapToSkillResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getDescription(),
                skill.getDifficulty(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
