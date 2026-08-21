package com.learningpath.service;

import com.learningpath.dto.ProjectDto;
import com.learningpath.dto.UpdateUserProjectRequest;
import com.learningpath.entity.Project;
import com.learningpath.entity.UserProject;
import com.learningpath.entity.enums.ProjectStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.ProjectRepository;
import com.learningpath.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsForUser(UUID userId) {
        List<Project> allProjects = projectRepository.findAll();
        Map<UUID, UserProject> userProjectMap = new HashMap<>();

        if (userId != null) {
            userProjectRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                    .forEach(up -> userProjectMap.put(up.getProject().getId(), up));
        }

        return allProjects.stream()
                .map(project -> mapToDto(project, userProjectMap.get(project.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        UserProject userProject = null;
        if (userId != null) {
            userProject = userProjectRepository.findByUserIdAndProjectId(userId, projectId).orElse(null);
        }

        return mapToDto(project, userProject);
    }

    @Transactional
    public ProjectDto startOrUpdateProject(UUID userId, UUID projectId, UpdateUserProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        UserProject userProject = userProjectRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseGet(() -> UserProject.builder()
                        .userId(userId)
                        .project(project)
                        .status(ProjectStatus.IN_PROGRESS)
                        .progressPercentage(0)
                        .startedAt(Instant.now())
                        .build());

        if (request != null) {
            if (request.status() != null) {
                userProject.setStatus(request.status());
                if (request.status() == ProjectStatus.COMPLETED && userProject.getCompletedAt() == null) {
                    userProject.setCompletedAt(Instant.now());
                    userProject.setProgressPercentage(100);
                }
            }
            if (request.progressPercentage() != null) {
                userProject.setProgressPercentage(request.progressPercentage());
                if (request.progressPercentage() >= 100) {
                    userProject.setStatus(ProjectStatus.COMPLETED);
                    if (userProject.getCompletedAt() == null) {
                        userProject.setCompletedAt(Instant.now());
                    }
                }
            }
            if (request.completedMilestones() != null) {
                userProject.setCompletedMilestones(request.completedMilestones());
            }
            if (request.submissionUrl() != null) {
                userProject.setSubmissionUrl(request.submissionUrl().trim());
            }
            if (request.notes() != null) {
                userProject.setNotes(request.notes().trim());
            }
        }

        UserProject saved = userProjectRepository.save(userProject);
        log.info("[ProjectService] Updated project status for userId={}, projectId={}, status={}",
                userId, projectId, saved.getStatus());

        return mapToDto(project, saved);
    }

    private ProjectDto mapToDto(Project project, UserProject userProject) {
        List<String> techList = new ArrayList<>();
        if (project.getTechnologies() != null && !project.getTechnologies().isBlank()) {
            techList = Arrays.stream(project.getTechnologies().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        ProjectStatus status = userProject != null ? userProject.getStatus() : ProjectStatus.NOT_STARTED;
        Integer progress = userProject != null ? userProject.getProgressPercentage() : 0;
        Instant startedAt = userProject != null ? userProject.getStartedAt() : null;
        Instant completedAt = userProject != null ? userProject.getCompletedAt() : null;

        return new ProjectDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                techList,
                project.getDifficulty(),
                project.getEstimatedHours(),
                project.getMilestonesCount(),
                project.getRepositoryTemplateUrl(),
                status,
                progress,
                startedAt,
                completedAt
        );
    }
}
