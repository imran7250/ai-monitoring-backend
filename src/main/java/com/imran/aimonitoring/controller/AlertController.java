//package com.imran.aimonitoring.controller;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import com.imran.aimonitoring.entity.AlertRule;
//import com.imran.aimonitoring.entity.MonitoredService;
//import com.imran.aimonitoring.entity.ServiceStatus;
//import com.imran.aimonitoring.repository.AlertRuleRepository;
//import com.imran.aimonitoring.repository.MonitoredServiceRepository;
//
//@RestController
//@RequestMapping("/api/alerts")
//public class AlertController {
//
//    private final AlertRuleRepository alertRuleRepository;
//    private final MonitoredServiceRepository serviceRepository;
//
//    public AlertController(AlertRuleRepository alertRuleRepository,
//                           MonitoredServiceRepository serviceRepository) {
//        this.alertRuleRepository = alertRuleRepository;
//        this.serviceRepository = serviceRepository;
//    }
//
//    // 🔔 Create new alert rule (OWNERSHIP ENFORCED)
//    @PostMapping("/service/{serviceId}")
//    public AlertRule createAlertRule(
//            @PathVariable Long serviceId,
//            @RequestParam String name,
//            @RequestParam String triggerStatus) {
//
//        String email = getCurrentUserEmail();
//
//        MonitoredService service = serviceRepository.findById(serviceId)
//                .orElseThrow(() -> new RuntimeException("Service not found"));
//
//        if (!service.getProject().getOwner().getEmail().equals(email)) {
//            throw new AccessDeniedException("You do not own this service");
//        }
//
//        ServiceStatus statusEnum;
//
//        try {
//            statusEnum = ServiceStatus.valueOf(triggerStatus.toUpperCase());
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid trigger status");
//        }
//
////        AlertRule rule = AlertRule.builder()
////                .name(name)
////                .triggerStatus(statusEnum)
////                .service(service)
////                .enabled(true)
////                .createdAt(LocalDateTime.now())
////                .build();
////
////        return alertRuleRepository.save(rule);
//        
//        
//     // 🔥 DUPLICATE CHECK
//        alertRuleRepository
//            .findByServiceIdAndTriggerStatus(serviceId, statusEnum)
//            .ifPresent(r -> {
//                throw new RuntimeException("Alert already exists for this trigger on this service");
//            });
//
//        AlertRule rule = AlertRule.builder()
//                .name(name)
//                .triggerStatus(statusEnum)
//                .service(service)
//                .enabled(true)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        return alertRuleRepository.save(rule);
//
//        
//    }
//
//
//    // 📜 List all alert rules for a service (OWNERSHIP ENFORCED)
//    @GetMapping("/service/{serviceId}")
//    public List<AlertRule> getServiceAlertRules(@PathVariable Long serviceId) {
//
//        String email = getCurrentUserEmail(); // ✅ ADDED
//
//        MonitoredService service = serviceRepository.findById(serviceId)
//                .orElseThrow(() -> new RuntimeException("Service not found"));
//
//        // 🔒 OWNERSHIP CHECK
//        if (!service.getProject().getOwner().getEmail().equals(email)) {
//            throw new AccessDeniedException("You do not own this service");
//        }
//
//        //return alertRuleRepository.findByServiceIdAndEnabledTrue(serviceId);
//        return alertRuleRepository.findByServiceIdAndEnabledTrueOrderByCreatedAtDesc(serviceId);
//    }
//
//    // 🚫 Disable alert rule (OWNERSHIP ENFORCED)  
//    @PutMapping("/{ruleId}/disable")
//    public AlertRule disableRule(@PathVariable Long ruleId) {
//
//        String email = getCurrentUserEmail(); // ✅ ADDED
//
//        AlertRule rule = alertRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Alert rule not found"));
//
//        // 🔒 OWNERSHIP CHECK
//        if (!rule.getService()
//                 .getProject()
//                 .getOwner()
//                 .getEmail()
//                 .equals(email)) {
//            throw new AccessDeniedException("You do not own this alert rule");
//        }
//
//        rule.setEnabled(false);
//        return alertRuleRepository.save(rule);
//    }
//
//    // ✅ Enable alert rule (OWNERSHIP ENFORCED)
//    @PutMapping("/{ruleId}/enable")
//    public AlertRule enableRule(@PathVariable Long ruleId) {
//
//        String email = getCurrentUserEmail(); // ✅ ADDED
//
//        AlertRule rule = alertRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Alert rule not found"));
//
//        // 🔒 OWNERSHIP CHECK
//        if (!rule.getService()
//                 .getProject()
//                 .getOwner()
//                 .getEmail()
//                 .equals(email)) {
//            throw new AccessDeniedException("You do not own this alert rule");
//        }
//
//        rule.setEnabled(true);
//        return alertRuleRepository.save(rule);
//    }
//
//    // ❌ Delete alert rule (OWNERSHIP ENFORCED)
//    @DeleteMapping("/{ruleId}") 
//    public void deleteRule(@PathVariable Long ruleId) {
//
//        String email = getCurrentUserEmail(); // ✅ ADDED
//
//        AlertRule rule = alertRuleRepository.findById(ruleId)
//                .orElseThrow(() -> new RuntimeException("Alert rule not found"));
//
//        // 🔒 OWNERSHIP CHECK
//        if (!rule.getService()
//                 .getProject()
//                 .getOwner()
//                 .getEmail()
//                 .equals(email)) {
//            throw new AccessDeniedException("You do not own this alert rule");
//        }
//
//        alertRuleRepository.delete(rule);
//    }
//
//    
//    
//    @GetMapping("/all")
//    public List<AlertRule> getAllAlerts() {
//
//        String email = getCurrentUserEmail();
//
//        return alertRuleRepository.findAllByOwner(email);
//    }
//
//    
//    // ===== Helper (ADDED – REQUIRED) =====
//    private String getCurrentUserEmail() {
//        Authentication auth =
//                SecurityContextHolder.getContext().getAuthentication();
//        return auth.getName();
//    }
//}

package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.dto.AlertRuleResponse;
import com.imran.aimonitoring.entity.AlertRule;
import com.imran.aimonitoring.entity.ServiceStatus;
import com.imran.aimonitoring.security.SecurityUtil;
import com.imran.aimonitoring.service.AlertRuleService;

// ✅ IMPROVEMENT #7 — AlertController now uses AlertRuleService
//    instead of calling alertRuleRepository directly.
//
//    Before: Controller had direct repository access + duplicate
//            ownership check logic that also existed in AlertRuleService.
//            Any business logic change had to be made in TWO places.
//
//    After:  Controller delegates ALL logic to AlertRuleService.
//            Single source of truth. Clean separation of concerns.
//
// ✅ IMPROVEMENT #8 — Using SecurityUtil.getCurrentUserEmail()
//    instead of duplicating the same 3-line pattern.

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRuleService alertRuleService;

    // ✅ Only AlertRuleService injected — no direct repository access
    public AlertController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    // 🔔 Create new alert rule
    @PostMapping("/service/{serviceId}")
    public AlertRule createAlertRule(
            @PathVariable Long serviceId,
            @RequestParam String name,
            @RequestParam String triggerStatus) {

        // ✅ Using SecurityUtil instead of duplicated code
        String email = SecurityUtil.getCurrentUserEmail();

        ServiceStatus statusEnum;
        try {
            statusEnum = ServiceStatus.valueOf(triggerStatus.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid trigger status. Use: UP, DOWN, DEGRADED");
        }

        // ✅ Delegates to service layer — ownership check happens inside service
        return alertRuleService.createRule(serviceId, name, statusEnum, email);
    }

    // 📜 List all alert rules for a service
    @GetMapping("/service/{serviceId}")
    public List<AlertRule> getServiceAlertRules(@PathVariable Long serviceId) {

        String email = SecurityUtil.getCurrentUserEmail();

        // ✅ Delegates to service layer
        return alertRuleService.getServiceRules(serviceId, email);
    }

    // 🚫 Disable alert rule
    @PutMapping("/{ruleId}/disable")
    public AlertRule disableRule(@PathVariable Long ruleId) {

        String email = SecurityUtil.getCurrentUserEmail();

        // ✅ Delegates to service layer
        return alertRuleService.updateRuleStatus(ruleId, false, email);
    }

    // ✅ Enable alert rule
    @PutMapping("/{ruleId}/enable")
    public AlertRule enableRule(@PathVariable Long ruleId) {

        String email = SecurityUtil.getCurrentUserEmail();

        // ✅ Delegates to service layer
        return alertRuleService.updateRuleStatus(ruleId, true, email);
    }

    // ❌ Delete alert rule
    @DeleteMapping("/{ruleId}")
    public void deleteRule(@PathVariable Long ruleId) {

        String email = SecurityUtil.getCurrentUserEmail();

        // ✅ Delegates to service layer
        alertRuleService.deleteRule(ruleId, email);
    }

    // 📋 Get all alerts for current user
//    @GetMapping("/all")
//    public List<AlertRule> getAllAlerts() {
//
//        String email = SecurityUtil.getCurrentUserEmail();
//
//        // ✅ Delegates to service layer
//        return alertRuleService.getAllRulesByOwner(email);
//    }
    
    
    @GetMapping("/all")
    public List<AlertRuleResponse> getAllAlerts() {

        String email = SecurityUtil.getCurrentUserEmail();

        return alertRuleService.getAllRulesByOwner(email);
    }
    
}