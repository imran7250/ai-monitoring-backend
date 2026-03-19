package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;

public class AlertRuleResponse {

    public Long id;
    public String name;
    public String triggerStatus;
    public Long serviceId;
    public String serviceName;
    public boolean enabled;
    public LocalDateTime createdAt;

    public AlertRuleResponse(Long id,
                             String name,
                             String triggerStatus,
                             Long serviceId,
                             String serviceName,
                             boolean enabled,
                             LocalDateTime createdAt) {

        this.id = id;
        this.name = name;
        this.triggerStatus = triggerStatus;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }
}