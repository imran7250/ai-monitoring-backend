package com.imran.aimonitoring.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.imran.aimonitoring.dto.CreateProjectRequest;
import com.imran.aimonitoring.entity.Project;
import com.imran.aimonitoring.entity.User;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.ProjectRepository;
import com.imran.aimonitoring.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MonitoredServiceRepository monitoredServiceRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          MonitoredServiceRepository monitoredServiceRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.monitoredServiceRepository = monitoredServiceRepository;
    }

    public Project createProject(CreateProjectRequest request, String userEmail) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Project name is required");
        }

        // ✅ FIXED
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = new Project(
                request.getName(),
                request.getDescription(),
                owner
        );

        return projectRepository.save(project);
    }

    public List<Project> getMyProjects(String userEmail) {

        // ✅ FIXED
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return projectRepository.findByOwnerId(user.getId());
    }

    public Project getProjectById(Long projectId, String userEmail) {

        // ✅ FIXED
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        if (!project.getOwner().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not own this project");
        }

        return project;
    }

    @Transactional
    public void deleteProject(Long projectId, String userEmail) {

        // ✅ FIXED
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        if (!project.getOwner().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not own this project");
        }

        boolean hasServices = monitoredServiceRepository.existsByProjectId(projectId);

        if (hasServices) {
            throw new IllegalStateException(
                    "Cannot delete project with active services. Remove services first."
            );
        }

        projectRepository.delete(project);
        log.debug("Project {} deleted by user {}", projectId, userEmail);
    }
}