package com.imran.aimonitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnomalySummaryDTO {

    private long totalAnomalies;
    private long servicesAffected;
    private Double highestDeviation;
    private String mostUnstableService;
}