package com.learningpath.repository;

import com.learningpath.entity.SkillAlias;
import com.learningpath.entity.enums.SkillMappingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAlias, UUID> {
    Optional<SkillAlias> findByDatasetSkillNameIgnoreCase(String datasetSkillName);
    boolean existsByDatasetSkillNameIgnoreCase(String datasetSkillName);
    List<SkillAlias> findByMappingType(SkillMappingType mappingType);
}
