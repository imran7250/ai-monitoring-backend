package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.dto.DashboardSummaryResponse;
import com.imran.aimonitoring.dto.RecentIncidentResponse;
import com.imran.aimonitoring.dto.RecentNotificationResponse;
import com.imran.aimonitoring.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 📊 Summary cards
//    @GetMapping("/summary")  
//    public DashboardSummaryResponse getSummary(String userEmail) {
//        return dashboardService.getSummary(userEmail);
//    }
    
    
    
    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return dashboardService.getSummary(email, isAdmin);
    }
    

    // 🔴 Recent incidents
    //@GetMapping("/incidents")
//    public List<RecentIncidentResponse> recentIncidents() {
//        return dashboardService.getRecentIncidents();
//    }
    
    @GetMapping("/incidents")
    public List<RecentIncidentResponse> recentIncidents(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return dashboardService.getRecentIncidents(email, isAdmin);
    }

    // 🔔 Recent notifications
//    @GetMapping("/notifications")
//    public List<RecentNotificationResponse> recentNotifications() {
//        return dashboardService.getRecentNotifications();
//    }
    
    @GetMapping("/notifications")
    public List<RecentNotificationResponse> recentNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return dashboardService.getRecentNotifications(email, isAdmin);
    }
    
    
}
 