package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredServiceResponse {

    private Long id;
    private String name;
    private String baseUrl;
    private String type;
    private String status;
    private LocalDateTime lastCheckedAt;
}
