package com.imran.aimonitoring.dto;



import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class MetricRequest {

    @NotNull(message = "serviceId is required")
    private Long serviceId;

    private double cpuUsage;
    private double memoryUsage;
    private double latencyMs;
    private double errorRate;
}
