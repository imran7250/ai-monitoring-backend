package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIStatusDTO {

    private boolean hasAnomaly;
    private LocalDateTime lastAnomalyTime;
    private String severity;
}