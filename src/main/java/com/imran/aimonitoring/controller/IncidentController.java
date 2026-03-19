


package com.imran.aimonitoring.controller;

import com.imran.aimonitoring.dto.RecentIncidentResponse;
import com.imran.aimonitoring.entity.Incident;
import com.imran.aimonitoring.service.IncidentService;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    // 🔴 Get OPEN incidents (Dashboard / Incidents page)
    @GetMapping("/open")
    public List<RecentIncidentResponse> getOpenIncidents(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return incidentService.getOpenIncidents(email, isAdmin);
    }

    // 📜 Incident history for a specific service
    @GetMapping("/service/{serviceId}")
    public List<Incident> getServiceIncidents(@PathVariable Long serviceId) {

        String email = getCurrentUserEmail();
        return incidentService.getServiceIncidents(serviceId, email);
    }

    // ✅ Manually resolve an incident
    @PutMapping("/{id}/resolve")
    public Incident resolveIncident(
            @PathVariable Long id,
            Authentication auth
    ) {

        String email = auth.getName();

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return incidentService.manualResolve(id, email, isAdmin);
    }

    // 📄 Get single incident
    @GetMapping("/{id}")
    public Incident getIncident(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return incidentService.getIncidentById(id, email, isAdmin);
    }

    // 👍 Acknowledge incident
    @PutMapping("/{id}/acknowledge")
    public Incident acknowledgeIncident(
            @PathVariable Long id,
            Authentication auth
    ) {

        String email = auth.getName();

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return incidentService.acknowledgeIncident(id, email, isAdmin);
    }

    // Helper
    private String getCurrentUserEmail() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}