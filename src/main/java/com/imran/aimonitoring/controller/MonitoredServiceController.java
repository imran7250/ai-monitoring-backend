package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.dto.CreateMonitoredServiceRequest;
import com.imran.aimonitoring.dto.MonitoredServiceResponse;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.service.MonitoredServiceService;

@RestController
@RequestMapping("/api/projects/{projectId}/services")
public class MonitoredServiceController {    

    private final MonitoredServiceService serviceService;

    public MonitoredServiceController(MonitoredServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // ✅ Add service to project (UNCHANGED)
    @PostMapping
    public MonitoredServiceResponse addService(
            @PathVariable Long projectId,
            @RequestBody CreateMonitoredServiceRequest request) {

        MonitoredService service = MonitoredService.builder()
                .name(request.getName())
                .baseUrl(request.getBaseUrl())
                .type(request.getType())
                .build();

        MonitoredService saved = serviceService.createService(projectId, service);

        return mapToResponse(saved);
    }

    // 🔒 List project services (OWNERSHIP ENFORCED)
    @GetMapping
    public List<MonitoredServiceResponse> getProjectServices(
            @PathVariable Long projectId) {

        // ✅ ADDED (minimal)
        String email = getCurrentUserEmail();

        // ✅ CHANGED CALL (ownership-aware)
        return serviceService.getProjectServices(projectId, email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ===== Helper (UNCHANGED) =====
    private MonitoredServiceResponse mapToResponse(MonitoredService s) {
        return new MonitoredServiceResponse(
                s.getId(),
                s.getName(),
                s.getBaseUrl(),
                s.getType().name(),
                s.getStatus().name(),
                s.getLastCheckedAt()
        );  
    }

    // ===== Helper (ADDED – REQUIRED) =====
    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
