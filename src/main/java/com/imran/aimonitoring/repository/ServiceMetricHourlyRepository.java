//package com.imran.aimonitoring.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//
//import com.imran.aimonitoring.entity.ServiceMetricHourly;
//
//public interface ServiceMetricHourlyRepository
//        extends JpaRepository<ServiceMetricHourly, Long> {
//	
//	@Modifying
//	@Query("DELETE FROM ServiceMetricHourly m WHERE m.serviceId = :serviceId")
//	void deleteByServiceId(Long serviceId);
//	
//}

package com.imran.aimonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.ServiceMetricHourly;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface ServiceMetricHourlyRepository
        extends JpaRepository<ServiceMetricHourly, Long> {

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceMetricHourly m WHERE m.serviceId = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);
}