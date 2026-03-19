package com.imran.aimonitoring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.MetricBaseline;

public interface MetricBaselineRepository extends JpaRepository<MetricBaseline, Long> {

    Optional<MetricBaseline> findByServiceIdAndMetricName(Long serviceId, String metricName);
}