//package com.imran.aimonitoring.service;
//
//import java.time.LocalDateTime;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.imran.aimonitoring.repository.NotificationRepository;
 //
//@Service
//public class NotificationCleanupService {
//
//    private final NotificationRepository notificationRepository;
//
//    public NotificationCleanupService(NotificationRepository notificationRepository) {
//        this.notificationRepository = notificationRepository;
//    }
//
//    @Scheduled(cron = "0 0 3 * * *") // every day 3 AM
//    @Transactional
//    public void cleanupNotifications() {
//
//        LocalDateTime failedCutoff = LocalDateTime.now().minusDays(7);
//        LocalDateTime sentCutoff = LocalDateTime.now().minusDays(30);
//
//        int deleted = notificationRepository.cleanupOldNotifications(
//                failedCutoff,
//                sentCutoff
//        );
//
//        System.out.println("🧹 Notification cleanup completed. Deleted = " + deleted);
//    }
//}


package com.imran.aimonitoring.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.repository.NotificationRepository;

// ✅ IMPROVEMENT #2 — Replaced System.out.println with proper Logger

@Service
public class NotificationCleanupService {

    // ✅ Proper Logger
    private static final Logger log = LoggerFactory.getLogger(NotificationCleanupService.class);

    private final NotificationRepository notificationRepository;

    public NotificationCleanupService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
    @Transactional
    public void cleanupNotifications() {

        LocalDateTime failedCutoff = LocalDateTime.now().minusDays(7);
        LocalDateTime sentCutoff   = LocalDateTime.now().minusDays(30);

        int deleted = notificationRepository.cleanupOldNotifications(
                failedCutoff,
                sentCutoff
        );

        // ✅ Use logger instead of System.out.println
        log.info("Notification cleanup completed. Deleted={}", deleted);
    }
}