//
//
//package com.imran.aimonitoring.scheduler;
//
//import java.time.LocalDateTime;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.imran.aimonitoring.repository.ServiceLogRepository;
//import com.imran.aimonitoring.repository.ServiceMetricRepository;
//
//@Service
//public class DataRetentionScheduler {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(DataRetentionScheduler.class);
//
//    // 7-day retention window (easy to change later)
//    private static final int RETENTION_DAYS = 7;
//
//    private final ServiceMetricRepository metricRepo;
//    private final ServiceLogRepository logRepo;
//
//    public DataRetentionScheduler(ServiceMetricRepository metricRepo,
//                                   ServiceLogRepository logRepo) {
//        this.metricRepo = metricRepo;
//        this.logRepo = logRepo;
//    }
//
//    /**
//     * Runs daily at 3 AM
//     * Cleans up metrics and logs older than RETENTION_DAYS
//     */
//    @Transactional
//    @Scheduled(cron = "0 0 3 * * ?")
//    public void cleanupOldData() {
//
//        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
//
//        long metricsDeleted = metricRepo.deleteByRecordedAtBefore(cutoff);
//        long logsDeleted = logRepo.deleteByCheckedAtBefore(cutoff);
//
//        if (metricsDeleted > 0 || logsDeleted > 0) {
//            log.info("Data retention executed. Deleted metrics: {}, Deleted logs: {}",
//                    metricsDeleted, logsDeleted);
//        } else {
//            log.debug("Data retention executed. No records eligible for deletion.");
//        }
//    }
//}

package com.imran.aimonitoring.scheduler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.aimonitoring.repository.ServiceLogRepository;
import com.imran.aimonitoring.repository.ServiceMetricRepository;

// ✅ IMPROVEMENT #2 — Already used Logger correctly, no changes needed.
//    Keeping this file clean and consistent with the rest of the codebase.

@Service
public class DataRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionScheduler.class);

    private static final int RETENTION_DAYS = 7;

    private final ServiceMetricRepository metricRepo;
    private final ServiceLogRepository logRepo;

    public DataRetentionScheduler(ServiceMetricRepository metricRepo,
                                   ServiceLogRepository logRepo) {
        this.metricRepo = metricRepo;
        this.logRepo    = logRepo;
    }

    // Runs daily at 3 AM
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldData() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);

        long metricsDeleted = metricRepo.deleteByRecordedAtBefore(cutoff);
        long logsDeleted    = logRepo.deleteByCheckedAtBefore(cutoff);

        if (metricsDeleted > 0 || logsDeleted > 0) {
            log.info("Data retention executed. Deleted metrics={}, logs={}",
                    metricsDeleted, logsDeleted);
        } else {
            log.debug("Data retention executed. No records eligible for deletion.");
        }
    }
}