



package com.imran.aimonitoring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.AlertRule;
import com.imran.aimonitoring.entity.ServiceStatus;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByServiceIdAndEnabledTrueOrderByCreatedAtDesc(Long serviceId);

    List<AlertRule> findByServiceIdAndTriggerStatusAndEnabledTrue(
            Long serviceId, ServiceStatus triggerStatus);

//    @Query("""
//        SELECT a FROM AlertRule a
//        JOIN a.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE u.email = :email
//        ORDER BY a.createdAt DESC
//    """)
//    List<AlertRule> findAllByOwner(@Param("email") String email);


 // ✅ FIX — JOIN FETCH prevents N+1 when service.getName() is called downstream
    @Query("""
        SELECT a FROM AlertRule a
        JOIN FETCH a.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE u.email = :email
        ORDER BY a.createdAt DESC
    """)
    List<AlertRule> findAllByOwner(@Param("email") String email);
    
    Optional<AlertRule> findByServiceIdAndTriggerStatus(Long serviceId, ServiceStatus triggerStatus);

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM AlertRule a WHERE a.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);
}