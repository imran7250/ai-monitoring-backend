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
//import com.imran.aimonitoring.entity.Notification;
//import com.imran.aimonitoring.entity.NotificationStatus;
//
//public interface NotificationRepository extends JpaRepository<Notification, Long> {
//	
//	
//	List<Notification> findByStatusOrderBySentAtDesc(NotificationStatus status);
//
//    // Service notifications (latest first)
//    List<Notification> findByServiceIdOrderBySentAtDesc(Long serviceId);
//
//    // Latest notification for service
//    Optional<Notification> findTopByServiceIdOrderBySentAtDesc(Long serviceId);
//
//    // Filter by status
//    List<Notification> findByStatus(NotificationStatus status);
//
//    // GLOBAL notifications (latest first)  ✅ ADD THIS
//    List<Notification> findAllByOrderBySentAtDesc();
//
//    // GLOBAL top 5
//    List<Notification> findTop5ByOrderBySentAtDesc();
//
//    // OWNER top 5
//    @Query("""
//        SELECT n
//        FROM Notification n
//        JOIN n.service s
//        JOIN s.project p
//        JOIN p.owner u
//        WHERE u.email = :email
//        ORDER BY n.sentAt DESC
//    """)
//    List<Notification> findTop5ByOwnerEmailOrderBySentAtDesc(
//            @Param("email") String email
//    );
//
//    // Delete when service removed
//    @Modifying
//    @Query("DELETE FROM Notification n WHERE n.service.id = :serviceId")
//    void deleteByServiceId(Long serviceId);
//
//    // Clean up query
//    
//    
//    @Modifying
//    @Query("""
//    DELETE FROM Notification n
//    WHERE
//    (n.status = com.imran.aimonitoring.entity.NotificationStatus.FAILED
//     AND n.sentAt < :failedCutoff)
//    OR
//    (n.status = com.imran.aimonitoring.entity.NotificationStatus.SENT
//     AND n.sentAt < :sentCutoff)
//    """)
//    int cleanupOldNotifications(
//            @Param("failedCutoff") LocalDateTime failedCutoff,
//            @Param("sentCutoff") LocalDateTime sentCutoff
//    );
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

import com.imran.aimonitoring.entity.Notification;
import com.imran.aimonitoring.entity.NotificationStatus;

// ✅ IMPROVEMENT #1 — Added @Transactional to all @Modifying methods.

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatusOrderBySentAtDesc(NotificationStatus status);

    List<Notification> findByServiceIdOrderBySentAtDesc(Long serviceId);

    Optional<Notification> findTopByServiceIdOrderBySentAtDesc(Long serviceId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findAllByOrderBySentAtDesc();

    List<Notification> findTop5ByOrderBySentAtDesc();

    @Query("""
        SELECT n
        FROM Notification n
        JOIN n.service s
        JOIN s.project p
        JOIN p.owner u
        WHERE u.email = :email
        ORDER BY n.sentAt DESC
    """)
    List<Notification> findTop5ByOwnerEmailOrderBySentAtDesc(
            @Param("email") String email
    );

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.service.id = :serviceId")
    void deleteByServiceId(@Param("serviceId") Long serviceId);

    // ✅ FIX: Added @Transactional
    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Notification n
    WHERE
    (n.status = com.imran.aimonitoring.entity.NotificationStatus.FAILED
     AND n.sentAt < :failedCutoff)
    OR
    (n.status = com.imran.aimonitoring.entity.NotificationStatus.SENT
     AND n.sentAt < :sentCutoff)
    """)
    int cleanupOldNotifications(
            @Param("failedCutoff") LocalDateTime failedCutoff,
            @Param("sentCutoff") LocalDateTime sentCutoff
    );
}