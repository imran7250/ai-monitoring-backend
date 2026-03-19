package com.imran.aimonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imran.aimonitoring.entity.MonitoredService;

public interface ServiceRepository extends JpaRepository<MonitoredService, Long> {

}