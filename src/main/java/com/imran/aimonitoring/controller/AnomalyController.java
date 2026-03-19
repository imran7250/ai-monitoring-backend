




package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.dto.AIStatusDTO;
import com.imran.aimonitoring.dto.AnomalyResponseDTO;
import com.imran.aimonitoring.dto.AnomalySummaryDTO;
import com.imran.aimonitoring.entity.AnomalyEvent;
import com.imran.aimonitoring.service.AnomalyQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyQueryService anomalyQueryService;

    // Service anomaly timeline
    @GetMapping("/service/{serviceId}")
    public List<AnomalyResponseDTO> getServiceAnomalies(@PathVariable Long serviceId) {
        return anomalyQueryService.getServiceAnomalies(serviceId);
    }

    // AI badge status
    @GetMapping("/service/{serviceId}/status")
    public AIStatusDTO getServiceAIStatus(@PathVariable Long serviceId) {
        return anomalyQueryService.getServiceAIStatus(serviceId);
    }

    // Dashboard AI summary
    @GetMapping("/summary")
    public AnomalySummaryDTO getDashboardSummary(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return anomalyQueryService.getDashboardSummary(email, isAdmin);
    }

    // Existing endpoint (KEEP)
    @GetMapping
    public List<AnomalyEvent> getRecentAnomalies(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return anomalyQueryService.getRecentAnomalies(email, isAdmin);
    }

    // New endpoint (MOVED TO /latest)
    @GetMapping("/latest")
    public List<AnomalyResponseDTO> getLatestAnomalies() {
        return anomalyQueryService.getLatestAnomalies();
    }
}