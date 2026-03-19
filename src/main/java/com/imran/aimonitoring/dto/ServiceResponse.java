package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceResponse {

    private Long id;
    private String name;
    private String baseUrl;
    private String type;
    private String status;
    private LocalDateTime lastCheckedAt;
    
    private Long projectId;   // 🔥 ADD THIS
}
