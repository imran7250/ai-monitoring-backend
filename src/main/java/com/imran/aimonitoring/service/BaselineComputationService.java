package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.imran.aimonitoring.entity.MetricBaseline;
import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.repository.MetricBaselineRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaselineComputationService {

    private final ServiceMetricRepository metricsRepo;
    private final MetricBaselineRepository baselineRepo;

    public void computeBaseline(Long serviceId) {

        // last 6 hours metrics
       // LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
        LocalDateTime window = LocalDateTime.now().minusHours(24);

        List<ServiceMetric> metrics =
                metricsRepo.findByServiceIdAndRecordedAtAfter(serviceId, window);

        if (metrics == null || metrics.size() < 10) return;

        computeMetric(serviceId, "cpu_usage",
                metrics.stream().map(ServiceMetric::getCpuUsage).collect(Collectors.toList()));

        computeMetric(serviceId, "memory_usage",
                metrics.stream().map(ServiceMetric::getMemoryUsage).collect(Collectors.toList()));

        computeMetric(serviceId, "latency_ms",
                metrics.stream().map(ServiceMetric::getLatencyMs).collect(Collectors.toList()));

        computeMetric(serviceId, "error_rate",
                metrics.stream().map(ServiceMetric::getErrorRate).collect(Collectors.toList()));
    }

    private void computeMetric(Long serviceId, String metricName, List<Double> values) {

        if (values == null || values.size() < 30) return;

        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double std = calculateStdDev(values, avg);

        MetricBaseline baseline = baselineRepo
                .findByServiceIdAndMetricName(serviceId, metricName)
                .orElse(new MetricBaseline());

        baseline.setServiceId(serviceId);
        baseline.setMetricName(metricName);
        baseline.setAvgValue(avg);
        baseline.setStdDev(std);
        baseline.setLastUpdated(LocalDateTime.now());

        baselineRepo.save(baseline);
    }

    private double calculateStdDev(List<Double> values, double mean) {

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance);
    }
}