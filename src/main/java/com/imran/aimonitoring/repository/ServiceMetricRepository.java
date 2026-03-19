//package com.imran.aimonitoring.repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//
//import com.imran.aimonitoring.entity.ServiceMetric;
//
//public interface ServiceMetricRepository extends JpaRepository<ServiceMetric, Long> {
//
//    List<ServiceMetric> findTop50ByServiceIdOrderByRecordedAtDesc(Long serviceId);
//
//    List<ServiceMetric> findByServiceIdAndRecordedAtAfter(Long serviceId, LocalDateTime time);
//    
////    void deleteByRecordedAtBefore(LocalDateTime time);
//    
//    long deleteByRecordedAtBefore(LocalDateTime time);
//    
//    List<ServiceMetric> findByRecordedAtAfter(LocalDateTime time);
//    
//    
//    @Modifying
//    @Query("DELETE FROM ServiceMetric m WHERE m.serviceId = :serviceId")
//    void deleteByServiceId(Long serviceId);
//}


package com.imran.aimonitoring.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.ServiceMetric;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface ServiceMetricRepository extends JpaRepository<ServiceMetric, Long> {

    List<ServiceMetric> findTop50ByServiceIdOrderByRecordedAtDesc(Long serviceId);

    List<ServiceMetric> findByServiceIdAndRecordedAtAfter(Long serviceId, LocalDateTime time);

    long deleteByRecordedAtBefore(LocalDateTime time);

    List<ServiceMetric> findByRecordedAtAfter(LocalDateTime time);

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceMetric m WHERE m.serviceId = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);
}
