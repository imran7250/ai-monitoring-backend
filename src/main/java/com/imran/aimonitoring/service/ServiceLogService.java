package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceLog;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.ServiceLogRepository;

@Service
public class ServiceLogService {

    private final ServiceLogRepository logRepository;
    private final MonitoredServiceRepository serviceRepository;

    public ServiceLogService(ServiceLogRepository logRepository,
                             MonitoredServiceRepository serviceRepository) {
        this.logRepository = logRepository;
        this.serviceRepository = serviceRepository;
    }

    // ================= UNSECURED METHODS (internal use) =================

    @Transactional(readOnly = true)
    public List<ServiceLog> getServiceLogs(Long serviceId) {
        return logRepository.findTop100ByServiceIdOrderByCheckedAtDesc(serviceId);
    }

    @Transactional(readOnly = true)
    public List<ServiceLog> getLast24HoursLogs(Long serviceId) {
        return logRepository.findByServiceIdAndCheckedAtAfter(
                serviceId, LocalDateTime.now().minusHours(24));
    }

    public long getUpCount(Long serviceId) {
        return logRepository.countByServiceIdAndStatus(serviceId, ServiceStatus.UP);
    }

    public long getDownCount(Long serviceId) {
        return logRepository.countByServiceIdAndStatus(serviceId, ServiceStatus.DOWN);
    }

    // ================= OWNERSHIP-SECURED METHODS =================

    @Transactional(readOnly = true)
    public List<ServiceLog> getServiceLogs(Long serviceId, String userEmail) {

        // ✅ FIXED
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (!service.getProject()
                    .getOwner()
                    .getEmail()
                    .equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        return logRepository.findTop100ByServiceIdOrderByCheckedAtDesc(serviceId);
    }

    @Transactional(readOnly = true)
    public List<ServiceLog> getLast24HoursLogs(Long serviceId, String userEmail) {

        // ✅ FIXED
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (!service.getProject()
                    .getOwner()
                    .getEmail()
                    .equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        return logRepository.findByServiceIdAndCheckedAtAfter(
                serviceId,
                LocalDateTime.now().minusHours(24)
        );
    }
}