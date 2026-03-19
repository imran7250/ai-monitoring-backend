package com.imran.aimonitoring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.imran.aimonitoring.dto.DashboardSummaryResponse;
import com.imran.aimonitoring.dto.RecentIncidentResponse;
import com.imran.aimonitoring.dto.RecentNotificationResponse;
import com.imran.aimonitoring.entity.Incident;
import com.imran.aimonitoring.entity.IncidentStatus;
import com.imran.aimonitoring.entity.Notification;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.repository.IncidentRepository;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final MonitoredServiceRepository serviceRepo;
    private final IncidentRepository incidentRepo;
    private final NotificationRepository notificationRepo;

    public DashboardService(
            MonitoredServiceRepository serviceRepo,
            IncidentRepository incidentRepo,
            NotificationRepository notificationRepo) {

        this.serviceRepo = serviceRepo;
        this.incidentRepo = incidentRepo;
        this.notificationRepo = notificationRepo;
    }


    
    
    public DashboardSummaryResponse getSummary(String email, boolean isAdmin) {

        if (isAdmin) {
            return DashboardSummaryResponse.builder()
                    .totalServices(serviceRepo.count())
                    .upServices(serviceRepo.countByStatus(ServiceStatus.UP))
                    .downServices(serviceRepo.countByStatus(ServiceStatus.DOWN))
                    .degradedServices(serviceRepo.countByStatus(ServiceStatus.DEGRADED))
                    .openIncidents(incidentRepo.countByStatus(IncidentStatus.OPEN))
                    .build();
        }

        return DashboardSummaryResponse.builder()
                .totalServices(serviceRepo.countByProjectOwnerEmail(email))
                .upServices(serviceRepo.countByProjectOwnerEmailAndStatus(email, ServiceStatus.UP))
                .downServices(serviceRepo.countByProjectOwnerEmailAndStatus(email, ServiceStatus.DOWN))
                .degradedServices(serviceRepo.countByProjectOwnerEmailAndStatus(email, ServiceStatus.DEGRADED))
                .openIncidents(incidentRepo.countOpenByOwner(IncidentStatus.OPEN, email))
                .build();
    }
    
    

    // 🔹 RECENT INCIDENTS
//    public List<RecentIncidentResponse> getRecentIncidents() {
//
//        return incidentRepo.findAll() 
//                .stream()
//                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
//                .limit(5)
//                .map(i -> RecentIncidentResponse.builder()
//                        .incidentId(i.getId())
//                        .serviceId(i.getService().getId())
//                        .serviceName(i.getService().getName())
//                        .reason(i.getReason())
//                        .status(i.getStatus().name())
//                        .startedAt(i.getStartedAt())
//                        .build())
//                .collect(Collectors.toList());
//    }
  //  @Transactional(readOnly = true)
    public List<RecentIncidentResponse> getRecentIncidents(String email, boolean isAdmin) {

        List<Incident> incidents = isAdmin
                ? incidentRepo.findTop5ByOrderByStartedAtDesc()
                : incidentRepo.findTop5ByOwnerEmailOrderByStartedAtDesc(email);

        return incidents.stream()
                .limit(5)
                .map(i -> RecentIncidentResponse.builder()
                        .incidentId(i.getId())
                        .serviceId(i.getService().getId())
                        .serviceName(i.getService().getName())
                        .reason(i.getReason())
                        .status(i.getStatus().name())
                        .startedAt(i.getStartedAt())
                        .build())
                .toList();
    }
    


  //  @Transactional(readOnly = true)
    public List<RecentNotificationResponse> getRecentNotifications(
            String email,
            boolean isAdmin
    ) {

        List<Notification> notifications = isAdmin
                ? notificationRepo.findTop5ByOrderBySentAtDesc()
                : notificationRepo.findTop5ByOwnerEmailOrderBySentAtDesc(email);

        return notifications.stream()
                .map(n -> RecentNotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .status(n.getStatus().name())
                        .channelType(n.getChannelType().name())
                        .sentAt(n.getSentAt())
                        .build())
                .toList();
    }
}
