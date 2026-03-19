package com.imran.aimonitoring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.dto.MetricRequest;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

@Service
public class MetricService {

    private final ServiceMetricRepository metricRepo;
    private final MonitoredServiceRepository serviceRepo;
    private final AlertEngineService alertEngineService;
    private final AnomalyDetectionService anomalyDetectionService;

    public MetricService(ServiceMetricRepository metricRepo,
                         MonitoredServiceRepository serviceRepo,
                         AlertEngineService alertEngineService,
                         AnomalyDetectionService anomalyDetectionService) {
        this.metricRepo = metricRepo;
        this.serviceRepo = serviceRepo;
        this.alertEngineService = alertEngineService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Transactional
    public void ingest(MetricRequest request) {

        // ✅ FIXED — also returns null instead of throwing if service deleted
        //    during health check (race condition protection)
        MonitoredService service = serviceRepo.findById(request.getServiceId())
                .orElse(null);

        if (service == null) {
            return;
        }

        ServiceMetric metric = new ServiceMetric();
        metric.setServiceId(service.getId());
        metric.setCpuUsage(request.getCpuUsage());
        metric.setMemoryUsage(request.getMemoryUsage());
        metric.setLatencyMs(request.getLatencyMs());
        metric.setErrorRate(request.getErrorRate());

        metricRepo.save(metric);
        anomalyDetectionService.analyze(metric);
    }

    public List<ServiceMetric> getRecentMetrics(Long serviceId) {
        return metricRepo.findTop50ByServiceIdOrderByRecordedAtDesc(serviceId);
    }
}