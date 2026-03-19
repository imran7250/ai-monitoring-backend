
package com.imran.aimonitoring.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.imran.aimonitoring.entity.AnomalyEvent;
import com.imran.aimonitoring.entity.MetricBaseline;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.repository.AnomalyEventRepository;
import com.imran.aimonitoring.repository.MetricBaselineRepository;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

//    private static final long ANOMALY_COOLDOWN_SECONDS = 300;
	private static final long ANOMALY_COOLDOWN_SECONDS = 900;
    private static final double ANOMALY_SIGMA_THRESHOLD = 3.0; // Production safe

    private final MetricBaselineRepository baselineRepo;
    private final AnomalyEventRepository anomalyRepo;
    private final MonitoredServiceRepository serviceRepo;
    private final AlertEngineService alertEngineService;

    public void analyze(ServiceMetric metric) {

        if (metric == null) return;

        detect(metric.getServiceId(), "cpu_usage", metric.getCpuUsage());
        detect(metric.getServiceId(), "memory_usage", metric.getMemoryUsage());
        detect(metric.getServiceId(), "latency_ms", metric.getLatencyMs());
        detect(metric.getServiceId(), "error_rate", metric.getErrorRate());
    }

    private void detect(Long serviceId, String metricName, Double value) {

        if (serviceId == null || value == null) return;

        MetricBaseline baseline =
                baselineRepo.findByServiceIdAndMetricName(serviceId, metricName)
                        .orElse(null);

        if (baseline == null) return;

        // 🔥 SPECIAL CASE: stdDev = 0 (important for error_rate)
        if (baseline.getStdDev() == 0) {

            // If baseline is 0 and value becomes non-zero → anomaly
            if (!value.equals(baseline.getAvgValue())) {
                triggerAnomaly(serviceId, metricName, value, baseline, 999.0);
            }
            return;
        }

        double deviation =
                Math.abs(value - baseline.getAvgValue()) / baseline.getStdDev();

        if (deviation > ANOMALY_SIGMA_THRESHOLD) {
            triggerAnomaly(serviceId, metricName, value, baseline, deviation);
        }
    }

    private void triggerAnomaly(Long serviceId,
                                String metricName,
                                Double value,
                                MetricBaseline baseline,
                                double deviation) {

        // 🔥 Cooldown protection
        Optional<AnomalyEvent> last =
                anomalyRepo.findTopByServiceIdAndMetricNameOrderByDetectedAtDesc(
                        serviceId, metricName);

        if (last.isPresent()) {
            long seconds = Duration.between(
                    last.get().getDetectedAt(),
                    LocalDateTime.now()
            ).getSeconds();

            if (seconds < ANOMALY_COOLDOWN_SECONDS) return;
        }

        // ✅ Save anomaly
        AnomalyEvent event = new AnomalyEvent();
        event.setServiceId(serviceId);
        event.setMetricName(metricName);
        event.setActualValue(value);
        event.setExpectedValue(baseline.getAvgValue());
        event.setDeviationScore(deviation);
        event.setDetectedAt(LocalDateTime.now());
        event.setStatus("OPEN");

        anomalyRepo.save(event);

        // 🔥 Trigger alert engine
        MonitoredService service =
                serviceRepo.findById(serviceId).orElse(null);

        if (service != null) {
            alertEngineService.processAnomaly(
                    service,
                    "AI anomaly detected in " + metricName +
                            " (Deviation: " + String.format("%.2f", deviation) + "σ)"
            );
        }
    }
}













