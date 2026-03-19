package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.Project;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.AlertRuleRepository;
import com.imran.aimonitoring.repository.AnomalyEventRepository;
import com.imran.aimonitoring.repository.IncidentRepository;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.NotificationRepository;
import com.imran.aimonitoring.repository.ProjectRepository;
import com.imran.aimonitoring.repository.ServiceLogRepository;
import com.imran.aimonitoring.repository.ServiceMetricHourlyRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

@Service
public class MonitoredServiceService {

    private static final Logger log = LoggerFactory.getLogger(MonitoredServiceService.class);

    private final MonitoredServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;

    private final ServiceMetricRepository metricRepo;
    private final ServiceMetricHourlyRepository hourlyRepo;
    private final ServiceLogRepository logRepo;
    private final AnomalyEventRepository anomalyRepo;
    private final IncidentRepository incidentRepo;
    private final NotificationRepository notificationRepo;
    private final AlertRuleRepository alertRuleRepo;

    public MonitoredServiceService(
            MonitoredServiceRepository serviceRepository,
            ProjectRepository projectRepository,
            ServiceMetricRepository metricRepo,
            ServiceMetricHourlyRepository hourlyRepo,
            ServiceLogRepository logRepo,
            AnomalyEventRepository anomalyRepo,
            IncidentRepository incidentRepo,
            NotificationRepository notificationRepo,
            AlertRuleRepository alertRuleRepo) {

        this.serviceRepository = serviceRepository;
        this.projectRepository = projectRepository;
        this.metricRepo = metricRepo;
        this.hourlyRepo = hourlyRepo;
        this.logRepo = logRepo;
        this.anomalyRepo = anomalyRepo;
        this.incidentRepo = incidentRepo;
        this.notificationRepo = notificationRepo;
        this.alertRuleRepo = alertRuleRepo;
    }

    public MonitoredService createService(Long projectId, MonitoredService input) {

        // ✅ FIXED — now uses ResourceNotFoundException (returns 404 instead of 400)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        MonitoredService service = MonitoredService.builder()
                .name(input.getName())
                .baseUrl(input.getBaseUrl())
                .type(input.getType())
                .project(project)
                .status(ServiceStatus.UNKNOWN)
                .createdAt(LocalDateTime.now())
                .lastCheckedAt(LocalDateTime.now())
                .build();

        // ✅ FIXED — removed duplicate setStatus and setCreatedAt calls
        //    These were already set in the builder above

        return serviceRepository.save(service);
    }

    public List<MonitoredService> getProjectServices(Long projectId) {

        // ✅ FIXED — replaced existsById + RuntimeException with ResourceNotFoundException
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }

        return serviceRepository.findByProjectId(projectId);
    }

    public MonitoredService getServiceById(Long serviceId, String userEmail) {

        // ✅ FIXED — now uses ResourceNotFoundException
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        String ownerEmail = service.getProject().getOwner().getEmail();

        if (userEmail == null || !ownerEmail.equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        return service;
    }

    public List<MonitoredService> getProjectServices(Long projectId, String userEmail) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        String ownerEmail = project.getOwner().getEmail();

        if (userEmail == null || !ownerEmail.equals(userEmail)) {
            throw new AccessDeniedException("You do not own this project");
        }

        return serviceRepository.findByProjectId(projectId);
    }

    @Transactional
    public void deleteService(Long serviceId, String userEmail) {

        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        String ownerEmail = service.getProject().getOwner().getEmail();

        log.debug("Logged user: {}", userEmail);
        log.debug("Owner user: {}", ownerEmail);

        if (userEmail == null || !ownerEmail.equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        alertRuleRepo.deleteByServiceId(serviceId);
        notificationRepo.deleteByServiceId(serviceId);
        incidentRepo.deleteByServiceId(serviceId);
        anomalyRepo.deleteByServiceId(serviceId);
        logRepo.deleteByServiceId(serviceId);
        metricRepo.deleteByServiceId(serviceId);
        hourlyRepo.deleteByServiceId(serviceId);

        serviceRepository.delete(service);
    }
}