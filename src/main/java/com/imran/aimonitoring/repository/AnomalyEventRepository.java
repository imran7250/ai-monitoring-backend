//
//
//package com.imran.aimonitoring.repository;
//
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
////import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.imran.aimonitoring.dto.AnomalyResponseDTO;
//import com.imran.aimonitoring.entity.AnomalyEvent;
//
//public interface AnomalyEventRepository extends JpaRepository<AnomalyEvent, Long> {
//
//    Optional<AnomalyEvent>
//    findTopByServiceIdAndMetricNameOrderByDetectedAtDesc(Long serviceId, String metricName);
//
//    List<AnomalyEvent>
//    findTop100ByServiceIdOrderByDetectedAtDesc(Long serviceId);
//
//    Optional<AnomalyEvent>
//    findTopByServiceIdAndStatusOrderByDetectedAtDesc(Long serviceId, String status);
//
//    List<AnomalyEvent>
//    findByDetectedAtAfter(LocalDateTime time);
//    
//    
//    
//    // Add this
//    @Query("""
//    	    SELECT a
//    	    FROM AnomalyEvent a
//    	    JOIN MonitoredService s ON s.id = a.serviceId
//    	    JOIN s.project p
//    	    JOIN p.owner u
//    	    WHERE a.detectedAt >= :time
//    	    AND u.email = :email
//    	""")
//    	List<AnomalyEvent> findRecentByOwner(
//    	        @Param("time") LocalDateTime time,
//    	        @Param("email") String email
//    	);
//    
//    
//    @Modifying
//    @Query("DELETE FROM AnomalyEvent a WHERE a.serviceId = :serviceId")
//    void deleteByServiceId(Long serviceId);
//    
//    
//
//    
//    
//    @Query("""
//            SELECT new com.imran.aimonitoring.dto.AnomalyResponseDTO(
//                a.serviceId,
//                s.name,
//                a.metricName,
//                a.actualValue,
//                a.expectedValue,
//                a.deviationScore,
//                a.detectedAt,
//                a.status
//            )
//            FROM AnomalyEvent a
//            JOIN MonitoredService s ON a.serviceId = s.id
//            ORDER BY a.detectedAt DESC
//            """)
//    List<AnomalyResponseDTO> findLatestAnomalies(Pageable pageable);
//}

package com.imran.aimonitoring.repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.dto.AnomalyResponseDTO;
import com.imran.aimonitoring.entity.AnomalyEvent;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.
//    Without @Transactional on @Modifying, Spring throws
//    TransactionRequiredException at runtime when deleting data.

public interface AnomalyEventRepository extends JpaRepository<AnomalyEvent, Long> {

    Optional<AnomalyEvent>
    findTopByServiceIdAndMetricNameOrderByDetectedAtDesc(Long serviceId, String metricName);

    List<AnomalyEvent>
    findTop100ByServiceIdOrderByDetectedAtDesc(Long serviceId);

    Optional<AnomalyEvent>
    findTopByServiceIdAndStatusOrderByDetectedAtDesc(Long serviceId, String status);

    List<AnomalyEvent>
    findByDetectedAtAfter(LocalDateTime time);

    @Query("""
            SELECT a
            FROM AnomalyEvent a
            JOIN MonitoredService s ON s.id = a.serviceId
            JOIN s.project p
            JOIN p.owner u
            WHERE a.detectedAt >= :time
            AND u.email = :email
        """)
    List<AnomalyEvent> findRecentByOwner(
            @Param("time") LocalDateTime time,
            @Param("email") String email
    );

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM AnomalyEvent a WHERE a.serviceId = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);

    @Query("""
            SELECT new com.imran.aimonitoring.dto.AnomalyResponseDTO(
                a.serviceId,
                s.name,
                a.metricName,
                a.actualValue,
                a.expectedValue,
                a.deviationScore,
                a.detectedAt,
                a.status
            )
            FROM AnomalyEvent a
            JOIN MonitoredService s ON a.serviceId = s.id
            ORDER BY a.detectedAt DESC
            """)
    List<AnomalyResponseDTO> findLatestAnomalies(Pageable pageable);
}