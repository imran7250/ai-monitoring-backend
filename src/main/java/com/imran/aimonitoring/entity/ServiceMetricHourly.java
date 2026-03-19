package com.imran.aimonitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(
    name = "service_metrics_hourly",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_service_hour",
            columnNames = {"serviceId", "hourBucket"}
        )
    },
    indexes = {
        @Index(name = "idx_hourly_service_time", columnList = "serviceId, hourBucket")
    }
)
@Getter
@Setter
public class ServiceMetricHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long serviceId;

    private double avgLatency;
    private double maxLatency;
    private double avgCpu;
    private double avgMemory;

    private LocalDateTime hourBucket;
}