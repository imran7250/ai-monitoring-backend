package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.imran.aimonitoring.entity.MetricBaseline;
import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.repository.MetricBaselineRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaselineTrainingService {

   // private static final int MIN_METRICS_REQUIRED = 50;
	private static final int MIN_METRICS_REQUIRED = 50;

    private final ServiceMetricRepository metricRepo;
    private final MetricBaselineRepository baselineRepo;

    public void trainBaseline(Long serviceId) {

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        List<ServiceMetric> metrics =
                metricRepo.findByServiceIdAndRecordedAtAfter(serviceId, last24h);

        if (metrics.size() < MIN_METRICS_REQUIRED) {
            return; // not enough data
        }

        computeAndSave(serviceId, "cpu_usage",
                metrics.stream().mapToDouble(ServiceMetric::getCpuUsage).toArray());

        computeAndSave(serviceId, "memory_usage",
                metrics.stream().mapToDouble(ServiceMetric::getMemoryUsage).toArray());

        computeAndSave(serviceId, "latency_ms",
                metrics.stream().mapToDouble(ServiceMetric::getLatencyMs).toArray());

        computeAndSave(serviceId, "error_rate",
                metrics.stream().mapToDouble(ServiceMetric::getErrorRate).toArray());
    }

    private void computeAndSave(Long serviceId, String metricName, double[] values) {

        double avg = java.util.Arrays.stream(values).average().orElse(0);

        double variance = java.util.Arrays.stream(values)
                .map(v -> Math.pow(v - avg, 2))
                .average()
                .orElse(0);

        double stdDev = Math.sqrt(variance);

        MetricBaseline baseline = baselineRepo
                .findByServiceIdAndMetricName(serviceId, metricName)
                .orElse(new MetricBaseline());

        baseline.setServiceId(serviceId);
        baseline.setMetricName(metricName);
        baseline.setAvgValue(avg);
        baseline.setStdDev(stdDev);

        baselineRepo.save(baseline);
    }
}