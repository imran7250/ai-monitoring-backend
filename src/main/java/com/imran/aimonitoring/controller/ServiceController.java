


package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.dto.CreateServiceRequest;
import com.imran.aimonitoring.dto.ServiceResponse;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceType;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.service.MonitoredServiceService;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final MonitoredServiceRepository serviceRepository;
    private final MonitoredServiceService monitoredServiceService;

    public ServiceController(
            MonitoredServiceRepository serviceRepository,
            MonitoredServiceService monitoredServiceService) {
        this.serviceRepository = serviceRepository;
        this.monitoredServiceService = monitoredServiceService;
    }

    // ✅ CREATE SERVICE
    @PostMapping
    public MonitoredService createService(@RequestBody CreateServiceRequest request) {

        MonitoredService input = MonitoredService.builder()
                .name(request.getName())
                .baseUrl(request.getBaseUrl())
                .type(ServiceType.valueOf(request.getType()))
                .build();

        return monitoredServiceService.createService(
                request.getProjectId(),
                input
        );
    }

    // ✅ GET ALL SERVICES FOR LOGGED USER
    @GetMapping
    public List<ServiceResponse> getServices() {

        var auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        return serviceRepository.findByProjectOwnerEmail(email)
                .stream()
                .map(service -> new ServiceResponse(
                        service.getId(),
                        service.getName(),
                        service.getBaseUrl(),
                        service.getType().name(),
                        service.getStatus().name(),
                        service.getLastCheckedAt(),
                        service.getProject().getId()
                ))
                .toList();
    }
    
    

    

    
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String email = userDetails.getUsername();

        monitoredServiceService.deleteService(serviceId, email);

        return ResponseEntity.noContent().build();
    }
    
    
}
