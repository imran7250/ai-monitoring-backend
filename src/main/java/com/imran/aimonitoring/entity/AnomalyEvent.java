//package com.imran.aimonitoring.entity;
//
//import java.time.LocalDateTime;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.Data;
//
//@Data
//@Entity
//@Table(name = "anomaly_events")
//public class AnomalyEvent {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Long serviceId;
//    private String metricName;
//    private Double actualValue;
//    private Double expectedValue;
//    private Double deviationScore;
//    private LocalDateTime detectedAt;
//    private String status;
//}

package com.imran.aimonitoring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

// ✅ IMPROVEMENT #10 — Added database indexes on frequently queried columns
//    Before: Every query on serviceId or detectedAt did a full table scan.
//    After:  Indexes make these queries fast even with millions of rows.
//
//    idx_anomaly_service_time — used by: getServiceAnomalies(), getServiceAIStatus()
//    idx_anomaly_status       — used by: findTopByServiceIdAndStatusOrderByDetectedAtDesc()

@Data
@Entity
@Table(
    name = "anomaly_events",
    indexes = {
        @Index(name = "idx_anomaly_service_time", columnList = "serviceId, detectedAt"),
        @Index(name = "idx_anomaly_status",       columnList = "status")
    }
)
public class AnomalyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long serviceId;
    private String metricName;
    private Double actualValue;
    private Double expectedValue;
    private Double deviationScore;
    private LocalDateTime detectedAt;
    private String status;
}