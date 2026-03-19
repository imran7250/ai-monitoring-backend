package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.dto.AlertRuleResponse;
import com.imran.aimonitoring.entity.AlertRule;
import com.imran.aimonitoring.entity.MonitoredService;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.exception.ResourceNotFoundException;
import com.imran.aimonitoring.repository.AlertRuleRepository;
import com.imran.aimonitoring.repository.MonitoredServiceRepository;

@Service
public class AlertRuleService {  

    private final AlertRuleRepository alertRuleRepository;
    private final MonitoredServiceRepository serviceRepository;

    public AlertRuleService(AlertRuleRepository alertRuleRepository,
                            MonitoredServiceRepository serviceRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public AlertRule createRule(Long serviceId,
                                String name,
                                ServiceStatus triggerStatus,
                                String userEmail) {

        // ✅ FIXED
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (!service.getProject().getOwner().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        alertRuleRepository
                .findByServiceIdAndTriggerStatus(serviceId, triggerStatus)
                .ifPresent(r -> {
                    throw new RuntimeException(
                            "Alert rule already exists for this trigger on this service");
                });

        AlertRule rule = AlertRule.builder()
                .name(name)
                .triggerStatus(triggerStatus)
                .service(service)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        return alertRuleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<AlertRule> getServiceRules(Long serviceId, String userEmail) {

        // ✅ FIXED
        MonitoredService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (!service.getProject().getOwner().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not own this service");
        }

        return alertRuleRepository.findByServiceIdAndEnabledTrueOrderByCreatedAtDesc(serviceId);
    }

    @Transactional(readOnly = true)
    public List<AlertRuleResponse> getAllRulesByOwner(String userEmail) {

        return alertRuleRepository.findAllByOwner(userEmail)
                .stream()
                .map(r -> new AlertRuleResponse(
                        r.getId(),
                        r.getName(),
                        r.getTriggerStatus().name(),
                        r.getService().getId(),
                        r.getService().getName(),
                        r.isEnabled(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public AlertRule updateRuleStatus(Long ruleId, boolean enabled, String userEmail) {

        // ✅ FIXED
        AlertRule rule = alertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", ruleId));

        if (!rule.getService()
                .getProject()
                .getOwner()
                .getEmail()
                .equals(userEmail)) {
            throw new AccessDeniedException("You do not own this alert rule");
        }

        rule.setEnabled(enabled);
        return alertRuleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long ruleId, String userEmail) {

        // ✅ FIXED
        AlertRule rule = alertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", ruleId));

        if (!rule.getService()
                .getProject()
                .getOwner()
                .getEmail()
                .equals(userEmail)) {
            throw new AccessDeniedException("You do not own this alert rule");
        }

        alertRuleRepository.delete(rule);
    }
}