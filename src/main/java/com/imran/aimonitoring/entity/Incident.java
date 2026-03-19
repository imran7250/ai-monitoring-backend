//package com.imran.aimonitoring.entity;
//
//import java.time.LocalDateTime;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//@Entity
//@Table(name = "incidents")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
//public class Incident {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // which monitored service failed
//    @JsonIgnore
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "service_id", nullable = false)
//    private MonitoredService service;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private IncidentStatus status;
//
//    @Column(nullable = false)
//    private LocalDateTime startedAt;
//
//    private LocalDateTime resolvedAt;
//
//    @Column(length = 1000)
//    private String reason;
//}


package com.imran.aimonitoring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ✅ IMPROVEMENT #10 — Added database indexes on frequently queried columns
//    idx_incident_service_status — used by: findByServiceAndStatus(),
//                                           findOpenByOwner(), countByStatus()
//    idx_incident_started_at     — used by: all ORDER BY startedAt DESC queries

@Entity
@Table(
    name = "incidents",
    indexes = {
        @Index(name = "idx_incident_service_status", columnList = "service_id, status"),
        @Index(name = "idx_incident_started_at",     columnList = "startedAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 1000)
    private String reason;
}