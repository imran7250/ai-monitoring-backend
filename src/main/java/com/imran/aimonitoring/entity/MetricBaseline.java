package com.imran.aimonitoring.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "metric_baselines",
       uniqueConstraints = @UniqueConstraint(columnNames = {"service_id","metric_name"}))
public class MetricBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "avg_value")
    private Double avgValue;

    @Column(name = "std_dev")
    private Double stdDev;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}