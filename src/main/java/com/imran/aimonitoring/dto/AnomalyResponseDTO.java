


package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnomalyResponseDTO {

    private Long serviceId;
    private String serviceName;
    private String metricName;
    private Double actualValue;
    private Double expectedValue;
    private Double deviationScore;
    private LocalDateTime detectedAt;
    private String status;
}
