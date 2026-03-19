




package com.imran.aimonitoring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.imran.aimonitoring.dto.RecentNotificationResponse;
import com.imran.aimonitoring.entity.Notification;
import com.imran.aimonitoring.entity.NotificationStatus;
import com.imran.aimonitoring.repository.NotificationRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 📬 All notifications (admin / audit)
    @GetMapping
    public List<RecentNotificationResponse> getAllNotifications() {

        return notificationRepository.findAllByOrderBySentAtDesc()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // 📜 Notifications for a service
    @GetMapping("/service/{serviceId}")
    public List<RecentNotificationResponse> getServiceNotifications(@PathVariable Long serviceId) {

        return notificationRepository.findByServiceIdOrderBySentAtDesc(serviceId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ❌ Failed notifications (debugging)
    @GetMapping("/failed")
    public List<RecentNotificationResponse> getFailedNotifications() {

        return notificationRepository
                .findByStatusOrderBySentAtDesc(NotificationStatus.FAILED)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // 🔁 ENTITY → DTO mapper
    private RecentNotificationResponse mapToDto(Notification n) {
        return RecentNotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .status(n.getStatus().name())
                .channelType(n.getChannelType().name())
                .sentAt(n.getSentAt())
                .build();
    }
}