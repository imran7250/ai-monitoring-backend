//package com.imran.aimonitoring.controller;
//
//import java.util.List;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import com.imran.aimonitoring.dto.CreateProjectRequest;
//import com.imran.aimonitoring.dto.ProjectResponse;
//import com.imran.aimonitoring.entity.Project;
//import com.imran.aimonitoring.service.ProjectService;
//
//@RestController
//@RequestMapping("/api/projects")
//public class ProjectController {
//
//    private final ProjectService projectService;
//
//    public ProjectController(ProjectService projectService) {
//        this.projectService = projectService;
//    }
//
//    // ✅ CREATE PROJECT (UNCHANGED)
//    @PostMapping
//    public ProjectResponse createProject(@RequestBody CreateProjectRequest request) {
//
//        String email = getCurrentUserEmail();
//
//        Project project = projectService.createProject(request, email);
//
//        return new ProjectResponse(
//                project.getId(),
//                project.getName(),
//                project.getDescription(),
//                project.getCreatedAt()
//        );
//    }
//
//    // ✅ LIST MY PROJECTS (UNCHANGED)
//    @GetMapping
//    public List<ProjectResponse> myProjects() {
//
//        String email = getCurrentUserEmail();
//
//        return projectService.getMyProjects(email)
//                .stream()
//                .map(p -> new ProjectResponse(
//                        p.getId(),
//                        p.getName(),
//                        p.getDescription(),
//                        p.getCreatedAt()))
//                .toList();
//    }
//
//    // 🔒 NEW: GET SINGLE PROJECT WITH OWNERSHIP CHECK
//    @GetMapping("/{projectId}")
//    public ProjectResponse getProjectById(@PathVariable Long projectId) {
//
//        String email = getCurrentUserEmail();
//
//        // 👇 THIS is step 2 + 3 in action
//        Project project = projectService.getProjectById(projectId, email);
//
//        return new ProjectResponse(
//                project.getId(),
//                project.getName(),
//                project.getDescription(),
//                project.getCreatedAt()
//        );
//    }
//
//    // ===== Helper (UNCHANGED) =====
//    private String getCurrentUserEmail() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return auth.getName();
//    }
//    
//    
//    
//    @DeleteMapping("/{projectId}")
//    public void deleteProject(@PathVariable Long projectId) {
//
//        String email = getCurrentUserEmail();
//
//        projectService.deleteProject(projectId, email);
//    }
//}  

package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.dto.CreateProjectRequest;
import com.imran.aimonitoring.dto.ProjectResponse;
import com.imran.aimonitoring.entity.Project;
import com.imran.aimonitoring.security.SecurityUtil;
import com.imran.aimonitoring.service.ProjectService;

import jakarta.validation.Valid;

// ✅ IMPROVEMENT #9 — Added @Valid to createProject()
// ✅ IMPROVEMENT #8 — Using SecurityUtil.getCurrentUserEmail()
//    instead of duplicating the 3-line SecurityContextHolder pattern

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ✅ @Valid triggers CreateProjectRequest validation automatically
    @PostMapping
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request) {

        // ✅ Using SecurityUtil instead of duplicated code
        String email = SecurityUtil.getCurrentUserEmail();

        Project project = projectService.createProject(request, email);

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt()
        );
    }

    @GetMapping
    public List<ProjectResponse> myProjects() {

        String email = SecurityUtil.getCurrentUserEmail();

        return projectService.getMyProjects(email)
                .stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProjectById(@PathVariable Long projectId) {

        String email = SecurityUtil.getCurrentUserEmail();

        Project project = projectService.getProjectById(projectId, email);

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt()
        );
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(@PathVariable Long projectId) {

        String email = SecurityUtil.getCurrentUserEmail();

        projectService.deleteProject(projectId, email);
    }
}