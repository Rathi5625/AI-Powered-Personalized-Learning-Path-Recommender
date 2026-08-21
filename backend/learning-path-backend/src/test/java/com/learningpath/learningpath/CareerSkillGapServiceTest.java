package com.learningpath.learningpath;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.learningpath.dto.SkillGapDetailDto;
import com.learningpath.learningpath.service.CareerSkillGapService;
import com.learningpath.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CareerSkillGapServiceTest {

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private LearnerKnowledgeStateRepository knowledgeStateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CareerSkillGapService gapService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .fullName("Test Learner")
                .email("learner@example.com")
                .targetCareer("Full Stack Developer")
                .build();
        user.setId(userId);
    }

    @Test
    void testAnalyzeGaps_CombinesBktAndVerifiedSkills() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Skill javaSkill = Skill.builder().name("Java").build();
        javaSkill.setId(UUID.randomUUID());

        UserSkill us = UserSkill.builder()
                .user(user)
                .skill(javaSkill)
                .proficiencyLevel(ProficiencyLevel.INTERMEDIATE)
                .build();
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(us));

        LearnerKnowledgeState bkt = LearnerKnowledgeState.builder()
                .conceptName("Spring Boot")
                .knowledgeProbability(0.78)
                .revisionRequired(false)
                .build();
        bkt.setId(UUID.randomUUID());
        when(knowledgeStateRepository.findByUserId(userId)).thenReturn(List.of(bkt));

        List<SkillGapDetailDto> gaps = gapService.analyzeGaps(userId, null);

        assertNotNull(gaps);
        assertFalse(gaps.isEmpty());

        SkillGapDetailDto springBootGap = gaps.stream()
                .filter(g -> g.getSkill().equalsIgnoreCase("Spring Boot"))
                .findFirst()
                .orElse(null);

        assertNotNull(springBootGap);
        assertEquals(0.78, springBootGap.getCurrentMastery());
        assertEquals("PROFICIENT", springBootGap.getStatus());
    }
}
