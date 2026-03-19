package com.imran.aimonitoring.dto;

import com.imran.aimonitoring.entity.ServiceType;
import lombok.Data;

@Data
public class CreateMonitoredServiceRequest {

    private String name;
    private String baseUrl;
    private ServiceType type;   // ✅ strong typing
}
