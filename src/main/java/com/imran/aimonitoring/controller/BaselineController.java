package com.imran.aimonitoring.controller;

import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.service.BaselineComputationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/baseline")
@RequiredArgsConstructor
public class BaselineController {

    private final BaselineComputationService baselineService;

    // Run baseline for a specific service
    @GetMapping("/run/{serviceId}")
    public String runBaseline(@PathVariable Long serviceId) {
        baselineService.computeBaseline(serviceId);
        return "Baseline computed for service: " + serviceId;
    }

    // Run baseline for all services (optional)
    @GetMapping("/run-all")
    public String runForAll() {
        // later we can auto fetch service ids
        return "Use /run/{serviceId} for now";
    }
}