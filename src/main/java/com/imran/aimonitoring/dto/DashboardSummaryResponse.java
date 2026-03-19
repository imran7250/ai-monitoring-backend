package com.imran.aimonitoring.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalServices;

    private long upServices;
    private long downServices;
    private long degradedServices;

    private long openIncidents;
}
