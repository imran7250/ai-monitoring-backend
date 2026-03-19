

package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.*;
import com.imran.aimonitoring.repository.AlertRuleRepository;
import com.imran.aimonitoring.repository.IncidentRepository;
import com.imran.aimonitoring.repository.NotificationRepository;

// ✅ BUG FIX #4 — resolveIncident() was silently losing data.
//
//    OLD CODE used findTopByServiceIdOrderByStartedAtDesc() which returns
//    the most recent incident by time — regardless of its status.
//
//    Example of the bug:
//      Service goes DOWN  → Incident #10 created (OPEN)
//      Service comes UP   → resolveIncident() runs
//      findTop...() returns Incident #9 (already RESOLVED from last week)
//      Code checks: is it ACKNOWLEDGED? No. Is it OPEN? No (it's RESOLVED).
//      Nothing happens. Incident #10 stays OPEN forever. ← BUG
//
//    FIX: Use findByServiceAndStatus(service, OPEN) — fetches the exact
//    OPEN incident for this service. If there's no OPEN incident, it returns
//    empty and we exit cleanly. No ambiguity, no silent skips.

@Service
public class AlertEngineService {

    private static final Logger log = LoggerFactory.getLogger(AlertEngineService.class);

    private final IncidentRepository incidentRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public AlertEngineService(IncidentRepository incidentRepository,
                              AlertRuleRepository alertRuleRepository,
                              NotificationService notificationService,
                              NotificationRepository notificationRepository) {
        this.incidentRepository = incidentRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void processStatusChange(MonitoredService service,
                                    ServiceStatus oldStatus,
                                    ServiceStatus newStatus,
                                    String errorMessage) {

        log.info("processStatusChange: service={} old={} new={}",
                service.getId(), oldStatus, newStatus);

        if (oldStatus == newStatus) return;

        Long serviceId = service.getId();

        // ================= INCIDENT LIFECYCLE =================

        if (newStatus == ServiceStatus.DOWN) {
            openIncident(service, errorMessage);
        }

        if (oldStatus == ServiceStatus.DOWN && newStatus == ServiceStatus.UP) {
            resolveIncident(service); // ✅ FIXED — see method below
        }

        // ================= ALERT RULES / NOTIFICATIONS =================

        List<AlertRule> rules =
                alertRuleRepository.findByServiceIdAndEnabledTrueOrderByCreatedAtDesc(serviceId);

        for (AlertRule rule : rules) {

            if (rule.getTriggerStatus() == newStatus) {

                // Duplicate notification protection for DOWN events
                if (newStatus == ServiceStatus.DOWN) {
                    Optional<Notification> last =
                            notificationRepository.findTopByServiceIdOrderBySentAtDesc(serviceId);

                    if (last.isPresent()) {
                        boolean alreadySent =
                                last.get().getStatus() == NotificationStatus.SENT &&
                                last.get().getMessage().contains("Status: DOWN");

                        if (alreadySent) continue;
                    }
                }

                String message = (newStatus == ServiceStatus.UP)
                        ? "Service recovered successfully"
                        : errorMessage;

                notificationService.sendNotifications(rule, service, message);
            }
        }
    }

    private void openIncident(MonitoredService service, String reason) {

        // Don't open a duplicate if one is already OPEN
        Optional<Incident> existing =
                incidentRepository.findByServiceAndStatus(service, IncidentStatus.OPEN);

        if (existing.isPresent()) {
            log.debug("Incident already open for service={}, skipping", service.getId());
            return;
        }

        Incident incident = Incident.builder()
                .service(service)
                .status(IncidentStatus.OPEN)
                .reason(reason)
                .startedAt(LocalDateTime.now())
                .build();

        incidentRepository.save(incident);
        log.info("Opened new incident for service={}", service.getId());
    }

    // ✅ BUG FIX #4 — Complete rewrite of this method
    private void resolveIncident(MonitoredService service) {

        // FIX: Query for the OPEN incident specifically, not just "most recent"
        // This prevents accidentally fetching an old RESOLVED incident
        Optional<Incident> optional =
                incidentRepository.findByServiceAndStatus(service, IncidentStatus.OPEN);

        // No open incident found — nothing to resolve, exit cleanly
        if (optional.isEmpty()) {
            log.debug("No open incident found for service={}, nothing to resolve", service.getId());
            return;
        }

        Incident incident = optional.get();

        // Respect ACKNOWLEDGED — engineer is reviewing it, they must resolve manually
        if (incident.getStatus() == IncidentStatus.ACKNOWLEDGED) {
            log.debug("Incident {} is ACKNOWLEDGED, skipping auto-resolve", incident.getId());
            return;
        }

        // Mark as resolved and SAVE — this save was missing in the original buggy code
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incidentRepository.save(incident); // ✅ THIS LINE WAS MISSING IN THE BUG

        log.info("Auto-resolved incident={} for service={}", incident.getId(), service.getId());
    }

    @Transactional
    public void processAnomaly(MonitoredService service, String message) {

        List<AlertRule> rules =
                alertRuleRepository.findByServiceIdAndEnabledTrueOrderByCreatedAtDesc(service.getId());

        for (AlertRule rule : rules) {
            if (rule.getTriggerStatus() == ServiceStatus.DEGRADED) {
                notificationService.sendNotifications(rule, service, message);
            }
        }
    }
}