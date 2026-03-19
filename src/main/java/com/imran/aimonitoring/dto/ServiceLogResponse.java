

package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLogResponse {

    private Long id;

    private Long serviceId;
    private String serviceName;

    private String status;
    private Long responseTimeMs;
    private String errorMessage;
    private LocalDateTime checkedAt;
}
