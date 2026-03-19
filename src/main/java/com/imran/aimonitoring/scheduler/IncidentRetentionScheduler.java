package com.imran.aimonitoring.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imran.aimonitoring.service.IncidentRetentionService;

@Component
public class IncidentRetentionScheduler {

    private final IncidentRetentionService retentionService;

    public IncidentRetentionScheduler(IncidentRetentionService retentionService) {
        this.retentionService = retentionService;
    }

    // Runs every day at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        retentionService.cleanupOldIncidents();
    }
}                          