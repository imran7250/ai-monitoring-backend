//package com.imran.aimonitoring.monitoring;
//
//import java.time.LocalDateTime;
//
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import com.imran.aimonitoring.entity.*;
//import com.imran.aimonitoring.repository.MonitoredServiceRepository;
//import com.imran.aimonitoring.repository.ServiceLogRepository;
//import com.imran.aimonitoring.service.IncidentService;
//
//@Component
//public class MonitoringScheduler {
//
//    private final MonitoredServiceRepository serviceRepository;
//    private final ServiceLogRepository logRepository;
//    private final IncidentService incidentService;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public MonitoringScheduler(MonitoredServiceRepository serviceRepository,
//                                ServiceLogRepository logRepository,
//                                IncidentService incidentService) {
//        this.serviceRepository = serviceRepository;
//        this.logRepository = logRepository;
//        this.incidentService = incidentService;
//    }
//
//    // ⏱ Runs every 60 seconds
//    @Scheduled(fixedRate = 60000)
//    public void checkAllServices() {
//
//        List<MonitoredService> services = serviceRepository.findAll();
//
//        for (MonitoredService service : services) {
//            checkService(service);
//        }
//    }
//
//    private void checkService(MonitoredService service) {
//
//        LocalDateTime now = LocalDateTime.now();
//        ServiceStatus oldStatus = service.getStatus(); // ✅ track previous state
//
//        ServiceStatus newStatus;
//        String errorMessage = null;
//
//        long startTime = System.currentTimeMillis();
//
//        try {
//            ResponseEntity<String> response =
//                    restTemplate.getForEntity(service.getBaseUrl(), String.class);
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                newStatus = ServiceStatus.UP;
//            } else {
//                newStatus = ServiceStatus.DEGRADED;
//                errorMessage = "Non-200 HTTP response";
//            }
//
//        } catch (Exception e) {
//            newStatus = ServiceStatus.DOWN;
//            errorMessage = e.getMessage();
//        }
//
//        long responseTime = System.currentTimeMillis() - startTime;
//
//        // ✅ Always save log (history)
//        ServiceLog log = ServiceLog.builder()
//                .service(service)
//                .status(newStatus)
//                .responseTimeMs(responseTime)
//                .errorMessage(errorMessage)
//                .checkedAt(now)
//                .build();
//
//        logRepository.save(log);
//
//        // ✅ Incident lifecycle only when state changes
//        if (oldStatus != newStatus) {
//
//            if (newStatus == ServiceStatus.UP) {
//                incidentService.handleServiceUp(service);
//            } else {
//                incidentService.handleServiceDown(service, errorMessage);
//            }
//        }
//
//        // ✅ Update live snapshot
//        service.setStatus(newStatus);
//        service.setLastCheckedAt(now);
//        serviceRepository.save(service);
//    }
//}
