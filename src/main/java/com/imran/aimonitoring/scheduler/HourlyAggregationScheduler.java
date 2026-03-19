

package com.imran.aimonitoring.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.entity.ServiceMetricHourly;
import com.imran.aimonitoring.repository.ServiceMetricHourlyRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

// ✅ IMPROVEMENT #2 — Replaced System.out.println with proper Logger

@Service
public class HourlyAggregationScheduler {

    // ✅ Proper Logger
    private static final Logger log = LoggerFactory.getLogger(HourlyAggregationScheduler.class);

    private final ServiceMetricRepository metricRepo;
    private final ServiceMetricHourlyRepository hourlyRepo;

    public HourlyAggregationScheduler(ServiceMetricRepository metricRepo,
                                      ServiceMetricHourlyRepository hourlyRepo) {
        this.metricRepo = metricRepo;
        this.hourlyRepo = hourlyRepo;
    }

//    @Scheduled(cron = "0 5 * * * ?") // every hour at minute 5
//    public void aggregateLastHour() {
//
//        LocalDateTime now         = LocalDateTime.now();
//        LocalDateTime oneHourAgo  = now.minusHours(1);
//
//        List<ServiceMetric> metrics = metricRepo.findByRecordedAtAfter(oneHourAgo);
//
//        metrics.stream()
//                .collect(java.util.stream.Collectors.groupingBy(ServiceMetric::getServiceId))
//                .forEach((serviceId, serviceMetrics) -> {
//
//                    double avgLatency = serviceMetrics.stream()
//                            .mapToDouble(ServiceMetric::getLatencyMs)
//                            .average().orElse(0);
//
//                    double maxLatency = serviceMetrics.stream()
//                            .mapToDouble(ServiceMetric::getLatencyMs)
//                            .max().orElse(0);
//
//                    double avgCpu = serviceMetrics.stream()
//                            .mapToDouble(ServiceMetric::getCpuUsage)
//                            .average().orElse(0);
//
//                    double avgMemory = serviceMetrics.stream()
//                            .mapToDouble(ServiceMetric::getMemoryUsage)
//                            .average().orElse(0);
//
//                    ServiceMetricHourly hourly = new ServiceMetricHourly();
//                    hourly.setServiceId(serviceId);
//                    hourly.setAvgLatency(avgLatency);
//                    hourly.setMaxLatency(maxLatency);
//                    hourly.setAvgCpu(avgCpu);
//                    hourly.setAvgMemory(avgMemory);
//                    hourly.setHourBucket(now.truncatedTo(ChronoUnit.HOURS));
//
//                    hourlyRepo.save(hourly);
//                });
//
//        // ✅ Use logger instead of System.out.println("Hourly metrics aggregated")
//        log.info("Hourly metrics aggregated for period ending={}", now);
//    }
    
    @Scheduled(cron = "0 5 * * * ?")
    public void aggregateLastHour() {

        LocalDateTime now        = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<ServiceMetric> metrics = metricRepo.findByRecordedAtAfter(oneHourAgo);

        if (metrics.isEmpty()) {
            log.debug("No metrics found for hourly aggregation");
            return;
        }

        // ✅ FIX — collect all records first, then saveAll() in one batch
        List<ServiceMetricHourly> toSave = new ArrayList<>();

        metrics.stream()
                .collect(java.util.stream.Collectors.groupingBy(ServiceMetric::getServiceId))
                .forEach((serviceId, serviceMetrics) -> {

                    double avgLatency = serviceMetrics.stream()
                            .mapToDouble(ServiceMetric::getLatencyMs)
                            .average().orElse(0);

                    double maxLatency = serviceMetrics.stream()
                            .mapToDouble(ServiceMetric::getLatencyMs)
                            .max().orElse(0);

                    double avgCpu = serviceMetrics.stream()
                            .mapToDouble(ServiceMetric::getCpuUsage)
                            .average().orElse(0);

                    double avgMemory = serviceMetrics.stream()
                            .mapToDouble(ServiceMetric::getMemoryUsage)
                            .average().orElse(0);

                    ServiceMetricHourly hourly = new ServiceMetricHourly();
                    hourly.setServiceId(serviceId);
                    hourly.setAvgLatency(avgLatency);
                    hourly.setMaxLatency(maxLatency);
                    hourly.setAvgCpu(avgCpu);
                    hourly.setAvgMemory(avgMemory);
                    hourly.setHourBucket(now.truncatedTo(ChronoUnit.HOURS));

                    toSave.add(hourly);
                });

        // ✅ One batch INSERT instead of N individual INSERTs
        hourlyRepo.saveAll(toSave);

        log.info("Hourly metrics aggregated for {} services ending={}", toSave.size(), now);
    }
}