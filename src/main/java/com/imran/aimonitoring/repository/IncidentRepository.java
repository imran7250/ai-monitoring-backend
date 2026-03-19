//
//
//
//
//
//package com.imran.aimonitoring.repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.imran.aimonitoring.entity.Incident;
//import com.imran.aimonitoring.entity.IncidentStatus;
//import com.imran.aimonitoring.entity.MonitoredService;
//
//public interface IncidentRepository extends JpaRepository<Incident, Long> {
//
//    // 🔹 Only ONE open incident per service
//    Optional<Incident> findByServiceAndStatus(
//            MonitoredService service,
//            IncidentStatus status
//    );
//
//    // 🔹 Open incidents by owner (DB-level filtering)
//    @Query("""
//        SELECT i FROM Incident i
//        JOIN i.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE i.status = :status
//        AND u.email = :email
//        ORDER BY i.startedAt DESC
//    """)
//    List<Incident> findOpenByOwner(
//            @Param("status") IncidentStatus status,
//            @Param("email") String email
//    );
//
//    // 🔹 Incident history of a service (with ownership)
//    @Query("""
//        SELECT i FROM Incident i
//        JOIN i.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE s.id = :serviceId
//        AND u.email = :email
//        ORDER BY i.startedAt DESC
//    """)
//    List<Incident> findServiceIncidentsForOwner(
//            @Param("serviceId") Long serviceId,
//            @Param("email") String email
//    );
//
//    // 🔹 Delete old resolved incidents (retention ready)
//    long deleteByStatusAndResolvedAtBefore(
//            IncidentStatus status,
//            LocalDateTime time
//    );
//    
////    List<Incident> findByStatus(IncidentStatus status);
//    
//    List<Incident> findByStatusOrderByStartedAtDesc(IncidentStatus status);
//    
//    
//    /////////
// // GLOBAL count
//    long countByStatus(IncidentStatus status);
//
//    // OWNER count
//    @Query("""
//        SELECT COUNT(i)
//        FROM Incident i
//        JOIN i.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE i.status = :status
//        AND u.email = :email
//    """)
//    long countOpenByOwner(
//            @Param("status") IncidentStatus status,
//            @Param("email") String email
//    );
//
//    // OWNER top 5 recent
//    @Query("""
//        SELECT i
//        FROM Incident i
//        JOIN i.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE u.email = :email
//        ORDER BY i.startedAt DESC
//    """)
//    List<Incident> findTop5ByOwnerEmailOrderByStartedAtDesc(
//            @Param("email") String email
//    );
//    
//    
//    //Add these
//    List<Incident> findTop5ByOrderByStartedAtDesc();
//    
//    
//    @Modifying
//    @Query("DELETE FROM Incident i WHERE i.service.id = :serviceId")
//    void deleteByServiceId(Long serviceId);
//    
//    
//    Optional<Incident> findTopByServiceIdOrderByStartedAtDesc(Long serviceId);
//    
//    List<Incident> findByStatusInOrderByStartedAtDesc(List<IncidentStatus> statuses);
//    
//    
//    @Query("""
//    		SELECT i
//    		FROM Incident i
//    		WHERE i.status IN :statuses
//    		AND i.service.project.owner.email = :email
//    		ORDER BY i.startedAt DESC
//    		""")
//    		List<Incident> findActiveByOwner(
//    		        List<IncidentStatus> statuses,
//    		        String email
//    		);
//    
//    
//    
//    
//    @Modifying
//    @Query("""
//    DELETE FROM Incident i
//    WHERE i.startedAt < :cutoff
//    """)
//    void deleteOlderThan(LocalDateTime cutoff);
//    
//    
//}


package com.imran.aimonitoring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.entity.Incident;
import com.imran.aimonitoring.entity.IncidentStatus;
import com.imran.aimonitoring.entity.MonitoredService;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByServiceAndStatus(
            MonitoredService service,
            IncidentStatus status
    );

    @Query("""
        SELECT i FROM Incident i
        JOIN i.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE i.status = :status
        AND u.email = :email
        ORDER BY i.startedAt DESC
    """)
    List<Incident> findOpenByOwner(
            @Param("status") IncidentStatus status,
            @Param("email") String email
    );

    @Query("""
        SELECT i FROM Incident i
        JOIN i.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE s.id = :serviceId
        AND u.email = :email
        ORDER BY i.startedAt DESC
    """)
    List<Incident> findServiceIncidentsForOwner(
            @Param("serviceId") Long serviceId,
            @Param("email") String email
    );

    long deleteByStatusAndResolvedAtBefore(
            IncidentStatus status,
            LocalDateTime time
    );

    List<Incident> findByStatusOrderByStartedAtDesc(IncidentStatus status);

    long countByStatus(IncidentStatus status);

    @Query("""
        SELECT COUNT(i)
        FROM Incident i
        JOIN i.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE i.status = :status
        AND u.email = :email
    """)
    long countOpenByOwner(
            @Param("status") IncidentStatus status,
            @Param("email") String email
    );

    @Query("""
        SELECT i
        FROM Incident i
        JOIN i.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE u.email = :email
        ORDER BY i.startedAt DESC
    """)
    List<Incident> findTop5ByOwnerEmailOrderByStartedAtDesc(
            @Param("email") String email
    );

    List<Incident> findTop5ByOrderByStartedAtDesc();

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM Incident i WHERE i.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);

    Optional<Incident> findTopByServiceIdOrderByStartedAtDesc(Long serviceId);

    List<Incident> findByStatusInOrderByStartedAtDesc(List<IncidentStatus> statuses);

    @Query("""
            SELECT i
            FROM Incident i
            WHERE i.status IN :statuses
            AND i.service.project.owner.email = :email
            ORDER BY i.startedAt DESC
            """)
    List<Incident> findActiveByOwner(
            List<IncidentStatus> statuses,
            String email
    );

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Incident i
    WHERE i.startedAt < :cutoff
    """)
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}