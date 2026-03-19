package com.imran.aimonitoring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceStatus;

public interface MonitoredServiceRepository 
        extends JpaRepository<MonitoredService, Long> {

    List<MonitoredService> findByProjectId(Long projectId);

    List<MonitoredService> findByStatus(ServiceStatus status);
    List<MonitoredService> findByProjectOwnerEmail(String email);
    
    
    // ✅ ADD THESE
    long countByStatus(ServiceStatus status);

    long countByProjectOwnerEmail(String email);

    long countByProjectOwnerEmailAndStatus(String email, ServiceStatus status);
     
    // 🔥 ADD THIS
    boolean existsByProjectId(Long projectId);


}
   