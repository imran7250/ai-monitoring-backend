//
//
//
//
//
//
//
//package com.imran.aimonitoring.scheduler;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.imran.aimonitoring.dto.MetricRequest;
//import com.imran.aimonitoring.entity.MonitoredService;
//import com.imran.aimonitoring.entity.ServiceLog;
//import com.imran.aimonitoring.entity.ServiceStatus;
//import com.imran.aimonitoring.repository.MonitoredServiceRepository;
//import com.imran.aimonitoring.repository.ServiceLogRepository;
//import com.imran.aimonitoring.service.AlertEngineService;
//import com.imran.aimonitoring.service.MetricService;
//
//@Service
//public class HealthCheckScheduler {
//
//    private static final int FAILURE_THRESHOLD = 3;
//	//private static final int FAILURE_THRESHOLD = 1; // for testing
//
//    private final MonitoredServiceRepository serviceRepo;
//    private final ServiceLogRepository logRepo;
//    private final MetricService metricService;
//    private final RestTemplate restTemplate;
//    private final AlertEngineService alertEngineService;
//
//    public HealthCheckScheduler(MonitoredServiceRepository serviceRepo,
//                                ServiceLogRepository logRepo,
//                                MetricService metricService,
//                                RestTemplate restTemplate,
//                                AlertEngineService alertEngineService) {
//        this.serviceRepo = serviceRepo;
//        this.logRepo = logRepo;
//        this.metricService = metricService;
//        this.restTemplate = restTemplate;
//        this.alertEngineService = alertEngineService;
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void checkServices() {
//
//        List<MonitoredService> services = serviceRepo.findAll();
//
////        services.parallelStream()
////                .forEach(this::checkSingleService);
//        
//        for (MonitoredService service : services) {
//            checkSingleService(service);
//        }
//    }
//
//    private void checkSingleService(MonitoredService service) {
//         
//        ServiceStatus oldStatus = service.getStatus();
//
//        long startTime = System.currentTimeMillis();
//        boolean success = false;
//        String errorMessage = null;
//
//        try {
//            restTemplate.getForEntity(service.getBaseUrl(), String.class);
//            success = true;
//        } catch (Exception ex) {
//            errorMessage = ex.getMessage();
//        }
//
//        long latency = System.currentTimeMillis() - startTime;
//
//        int failures = service.getConsecutiveFailures();
//
//        if (success) {
//            failures = 0;
//        } else {
//            failures++;
//        }
//
//        service.setConsecutiveFailures(failures);
//
//        // 🔥 Status depends ONLY on failure count
//        if (failures >= FAILURE_THRESHOLD) {
//            service.setStatus(ServiceStatus.DOWN);
//        } else {
//            service.setStatus(ServiceStatus.UP);
//        }
//
//        ServiceStatus newStatus = service.getStatus();
//
//        service.setLastCheckedAt(LocalDateTime.now());
////        serviceRepo.save(service);
//        serviceRepo.saveAndFlush(service);
//
//        if (oldStatus != newStatus) {
//            alertEngineService.processStatusChange(
//                    service,
//                    oldStatus,
//                    newStatus,
//                    errorMessage
//            );
//        }
//
//        ServiceLog log = ServiceLog.builder()
//                .service(service)
//                .status(newStatus)
//                .responseTimeMs(latency)
//                .errorMessage(errorMessage)
//                .checkedAt(LocalDateTime.now())
//                .build();
//
//        logRepo.save(log);
//
//        MetricRequest metric = new MetricRequest();
//        metric.setServiceId(service.getId());
//        metric.setCpuUsage(40 + Math.random() * 30);
//        metric.setMemoryUsage(50 + Math.random() * 25);
//        metric.setLatencyMs(latency);
//        metric.setErrorRate(newStatus == ServiceStatus.DOWN ? 100 : 0);
//
//        metricService.ingest(metric);
//      //  System.out.println("DEBUG VERSION 2 ACTIVE");
//    }
//}







package com.imran.aimonitoring.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.imran.aimonitoring.dto.MetricRequest;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceLog;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.ServiceLogRepository;
import com.imran.aimonitoring.service.AlertEngineService;
import com.imran.aimonitoring.service.MetricService;

// ✅ IMPROVEMENT #2 — Removed System.out.println("DEBUG VERSION 2 ACTIVE")
// ✅ IMPROVEMENT #3 — Removed fake Math.random() CPU/memory metrics
// ✅ IMPROVEMENT #4 — Switched from sequential loop to parallelStream()
//                     so multiple services are checked simultaneously

@Service
public class HealthCheckScheduler {

    // ✅ Use proper Logger instead of System.out.println
    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private static final int FAILURE_THRESHOLD = 3;

    private final MonitoredServiceRepository serviceRepo;
    private final ServiceLogRepository logRepo;
    private final MetricService metricService;
    private final RestTemplate restTemplate;
    private final AlertEngineService alertEngineService;

    public HealthCheckScheduler(MonitoredServiceRepository serviceRepo,
                                ServiceLogRepository logRepo,
                                MetricService metricService,
                                RestTemplate restTemplate,
                                AlertEngineService alertEngineService) {
        this.serviceRepo = serviceRepo;
        this.logRepo = logRepo;
        this.metricService = metricService;
        this.restTemplate = restTemplate;
        this.alertEngineService = alertEngineService;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkServices() {

        List<MonitoredService> services = serviceRepo.findAll();

        log.info("Starting health check for {} services", services.size());

        // ✅ IMPROVEMENT #4 — parallelStream checks multiple services at the same time
        // Previously: sequential loop — if 10 services × 3s timeout = 30s per cycle
        // Now: all services checked in parallel — cycle stays under 5s
        services.parallelStream().forEach(this::checkSingleService);

        log.info("Health check completed for {} services", services.size());
    }

    private void checkSingleService(MonitoredService service) {

        ServiceStatus oldStatus = service.getStatus();

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;

        try {
            restTemplate.getForEntity(service.getBaseUrl(), String.class);
            success = true;
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
        }

        long latency = System.currentTimeMillis() - startTime;

        int failures = service.getConsecutiveFailures();

        if (success) {
            failures = 0;
        } else {
            failures++;
        }

        service.setConsecutiveFailures(failures);

        if (failures >= FAILURE_THRESHOLD) {
            service.setStatus(ServiceStatus.DOWN);
        } else {
            service.setStatus(ServiceStatus.UP);
        }

        ServiceStatus newStatus = service.getStatus();
        service.setLastCheckedAt(LocalDateTime.now());
        serviceRepo.saveAndFlush(service);

        if (oldStatus != newStatus) {
            alertEngineService.processStatusChange(
                    service,
                    oldStatus,
                    newStatus,
                    errorMessage
            );
        }

        ServiceLog serviceLog = ServiceLog.builder()
                .service(service)
                .status(newStatus)
                .responseTimeMs(latency)
                .errorMessage(errorMessage)
                .checkedAt(LocalDateTime.now())
                .build();

        logRepo.save(serviceLog);

        // ✅ IMPROVEMENT #3 — Only use REAL metrics (latency from actual HTTP call)
        // REMOVED: fake Math.random() CPU and memory values that were
        //          corrupting the AI anomaly detection baselines
        MetricRequest metric = new MetricRequest();
        metric.setServiceId(service.getId());
        metric.setLatencyMs(latency);  // ← real measured latency
        metric.setErrorRate(newStatus == ServiceStatus.DOWN ? 100 : 0); // ← real

        // CPU and memory left as 0 until real metrics source is integrated
        // (e.g. Spring Actuator / Micrometer)
        metric.setCpuUsage(0);
        metric.setMemoryUsage(0);

        metricService.ingest(metric);

        // ✅ IMPROVEMENT #2 — Replaced System.out.println with proper logger
        log.debug("Health check done: service={} status={} latency={}ms",
                service.getId(), newStatus, latency);
    }
}
