package com.imran.aimonitoring.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentNotificationResponse {

    private Long id;
    private String message;
    private String status;
    private String channelType;
    private LocalDateTime sentAt;
}
