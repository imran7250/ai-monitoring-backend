package com.imran.aimonitoring.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imran.aimonitoring.entity.Notification;
import com.imran.aimonitoring.entity.NotificationStatus;
import com.imran.aimonitoring.repository.NotificationRepository;
import com.imran.aimonitoring.service.EmailSenderService;

@Component
public class NotificationRetryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationRetryScheduler.class);

    private static final int MAX_RETRY = 3;

    private final NotificationRepository notificationRepository;
    private final EmailSenderService emailSenderService;

    public NotificationRetryScheduler(
            NotificationRepository notificationRepository,
            EmailSenderService emailSenderService) {
        this.notificationRepository = notificationRepository;
        this.emailSenderService = emailSenderService;
    }

    @Scheduled(fixedDelay = 60000)
    public void retryFailedNotifications() {

        List<Notification> failed =
                notificationRepository.findByStatus(NotificationStatus.FAILED);

        log.debug("Retry scheduler running — {} failed notifications found", failed.size());

        for (Notification notification : failed) {

            if (notification.getRetryCount() >= MAX_RETRY) {
                continue;
            }

            if (!isRetryWindowOpen(notification)) {
                continue;
            }

            try {
                log.info("Retrying notification={} attempt={}",
                        notification.getId(), notification.getRetryCount() + 1);

                emailSenderService.sendEmail(
                        notification.getTarget(),
                        "RETRY ALERT",
                        notification.getMessage()
                );

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());

                log.info("Retry succeeded for notification={}", notification.getId());

            } catch (Exception e) {
                // ✅ FIX — was silent, now logged
                log.error("Retry failed for notification={} attempt={} error={}",
                        notification.getId(),
                        notification.getRetryCount() + 1,
                        e.getMessage());

                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setLastRetryAt(LocalDateTime.now());
            }

            notificationRepository.save(notification);
        }
    }

    private boolean isRetryWindowOpen(Notification notification) {

        if (notification.getLastRetryAt() == null) return true;

        int retry = notification.getRetryCount();

        long delayMinutes = switch (retry) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 5;
            default -> Long.MAX_VALUE;
        };

        if (delayMinutes == Long.MAX_VALUE) return false;

        return notification.getLastRetryAt()
                .plusMinutes(delayMinutes)
                .isBefore(LocalDateTime.now());
    }
}