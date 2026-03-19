//package com.imran.aimonitoring.repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//
//import com.imran.aimonitoring.entity.ServiceLog;
//
//public interface ServiceLogRepository extends JpaRepository<ServiceLog, Long> {
//
////    List<ServiceLog> findByServiceIdOrderByCheckedAtDesc(Long serviceId);
//	List<ServiceLog> findTop100ByServiceIdOrderByCheckedAtDesc(Long serviceId);
//    List<ServiceLog> findByServiceIdAndCheckedAtAfter(Long serviceId, LocalDateTime time);
//
//    long countByServiceIdAndStatus(Long serviceId, com.imran.aimonitoring.entity.ServiceStatus status);
//
////    void deleteByCheckedAtBefore(LocalDateTime time);
//    
//    long deleteByCheckedAtBefore(LocalDateTime time);
//    
//    
//    @Modifying
//    @Query("DELETE FROM ServiceLog l WHERE l.service.id = :serviceId")
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

import com.imran.aimonitoring.entity.ServiceLog;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface ServiceLogRepository extends JpaRepository<ServiceLog, Long> {

    List<ServiceLog> findTop100ByServiceIdOrderByCheckedAtDesc(Long serviceId);

    List<ServiceLog> findByServiceIdAndCheckedAtAfter(Long serviceId, LocalDateTime time);

    long countByServiceIdAndStatus(Long serviceId, com.imran.aimonitoring.entity.ServiceStatus status);

    long deleteByCheckedAtBefore(LocalDateTime time);

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceLog l WHERE l.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);
}
