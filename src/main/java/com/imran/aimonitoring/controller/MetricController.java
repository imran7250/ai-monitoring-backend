package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.dto.MetricRequest;
import com.imran.aimonitoring.entity.ServiceMetric;
import com.imran.aimonitoring.service.MetricService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping
    public String pushMetrics(@Valid @RequestBody MetricRequest request) {
        metricService.ingest(request);
        return "Metrics received";
    }

    @GetMapping("/service/{serviceId}")
    public List<ServiceMetric> recent(@PathVariable Long serviceId) {
        return metricService.getRecentMetrics(serviceId);
    }
}
 