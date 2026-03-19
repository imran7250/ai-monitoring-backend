package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentIncidentResponse {

    private Long incidentId;
    private Long serviceId;
    private String serviceName;
    private String reason;
    private String status;
    private LocalDateTime startedAt;
}
