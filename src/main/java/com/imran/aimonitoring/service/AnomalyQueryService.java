package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.imran.aimonitoring.dto.AIStatusDTO;
import com.imran.aimonitoring.dto.AnomalyResponseDTO;
import com.imran.aimonitoring.dto.AnomalySummaryDTO;
import com.imran.aimonitoring.entity.AnomalyEvent;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.repository.AnomalyEventRepository;
import com.imran.aimonitoring.repository.ServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnomalyQueryService {

    private final AnomalyEventRepository anomalyRepo;
    private final ServiceRepository serviceRepository;

    // Timeline for service
//    public List<AnomalyResponseDTO> getServiceAnomalies(Long serviceId) {
//
//        return anomalyRepo
//                .findTop100ByServiceIdOrderByDetectedAtDesc(serviceId)
//                .stream()
//
//                .map(e -> {
//
//                    String serviceName = serviceRepository
//                            .findById(e.getServiceId())
//                            .map(s -> s.getName())
//                            .orElse("Unknown Service");
//
//                    return new AnomalyResponseDTO(
//                            e.getServiceId(),
//                            serviceName,
//                            e.getMetricName(),
//                            e.getActualValue(),
//                            e.getExpectedValue(),
//                            e.getDeviationScore(),
//                            e.getDetectedAt(),
//                            e.getStatus()
//                    );
//                })
//                .collect(Collectors.toList());
//    }
    
    
    public List<AnomalyResponseDTO> getServiceAnomalies(Long serviceId) {

        // ✅ FIX — one lookup for the service name, not N lookups inside the stream
        String serviceName = serviceRepository.findById(serviceId)
                .map(MonitoredService::getName)
                .orElse("Unknown Service");

        return anomalyRepo
                .findTop100ByServiceIdOrderByDetectedAtDesc(serviceId)
                .stream()
                .map(e -> new AnomalyResponseDTO(
                        e.getServiceId(),
                        serviceName,
                        e.getMetricName(),
                        e.getActualValue(),
                        e.getExpectedValue(),
                        e.getDeviationScore(),
                        e.getDetectedAt(),
                        e.getStatus()
                ))
                .collect(Collectors.toList());
    }

    // AI badge status
    public AIStatusDTO getServiceAIStatus(Long serviceId) {

        return anomalyRepo
                .findTopByServiceIdAndStatusOrderByDetectedAtDesc(serviceId, "OPEN")
                .map(e -> new AIStatusDTO(
                        true,
                        e.getDetectedAt(),
                        e.getDeviationScore() > 5 ? "CRITICAL" : "HIGH"
                ))
                .orElse(new AIStatusDTO(false, null, "NONE"));
    }

   
//    
//    public AnomalySummaryDTO getDashboardSummary(String email, boolean isAdmin) {
//
//        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
//    	//LocalDateTime last24h = LocalDateTime.now().minusMinutes(5);
//
//        List<AnomalyEvent> recent = isAdmin
//                ? anomalyRepo.findByDetectedAtAfter(last24h)
//                : anomalyRepo.findRecentByOwner(last24h, email);
//
//        long total = recent.size();
//
//        long services = recent.stream()
//                .map(AnomalyEvent::getServiceId)
//                .distinct()
//                .count();
//
//        double maxDeviation = recent.stream()
//                .mapToDouble(AnomalyEvent::getDeviationScore)
//                .max()
//                .orElse(0);
//
//        String unstableService =
//                recent.stream()
//                        .collect(Collectors.groupingBy(
//                                AnomalyEvent::getServiceId,
//                                Collectors.counting()))
//                        .entrySet()
//                        .stream()
//                        .max((a,b) -> Long.compare(a.getValue(), b.getValue()))
//                        .map(e -> serviceRepository.findById(e.getKey())
//                                .map(s -> s.getName())
//                                .orElse("Unknown Service"))
//                        .orElse("None");
//
//        return new AnomalySummaryDTO(total, services, maxDeviation, unstableService);
//    }
//    
    
    
    public AnomalySummaryDTO getDashboardSummary(String email, boolean isAdmin) {

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        List<AnomalyEvent> recent = isAdmin
                ? anomalyRepo.findByDetectedAtAfter(last24h)
                : anomalyRepo.findRecentByOwner(last24h, email);

        long total = recent.size();

        long services = recent.stream()
                .map(AnomalyEvent::getServiceId)
                .distinct()
                .count();

        double maxDeviation = recent.stream()
                .mapToDouble(AnomalyEvent::getDeviationScore)
                .max()
                .orElse(0);

        // ✅ FIX — batch load all service names in one query, not N queries
        Set<Long> serviceIds = recent.stream()
                .map(AnomalyEvent::getServiceId)
                .collect(Collectors.toSet());

        Map<Long, String> nameMap = serviceRepository.findAllById(serviceIds)
                .stream()
                .collect(Collectors.toMap(
                        MonitoredService::getId,
                        MonitoredService::getName
                ));

        String unstableService = recent.stream()
                .collect(Collectors.groupingBy(
                        AnomalyEvent::getServiceId,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> nameMap.getOrDefault(e.getKey(), "Unknown Service"))
                .orElse("None");

        return new AnomalySummaryDTO(total, services, maxDeviation, unstableService);
    }
    
    public List<AnomalyEvent> getRecentAnomalies(String email, boolean isAdmin) {

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        return isAdmin
                ? anomalyRepo.findByDetectedAtAfter(last24h)
                : anomalyRepo.findRecentByOwner(last24h, email);
    }
    

    
    public List<AnomalyResponseDTO> getLatestAnomalies() {

        return anomalyRepo.findLatestAnomalies(
                PageRequest.of(0, 50) // latest 50 anomalies
        );
    }
    
    
}