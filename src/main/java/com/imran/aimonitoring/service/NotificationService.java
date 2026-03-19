//
//
//
//package com.imran.aimonitoring.service;
//
//import java.time.LocalDateTime;
//import java.util.Set;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.imran.aimonitoring.entity.*;
//import com.imran.aimonitoring.repository.NotificationChannelRepository;
//import com.imran.aimonitoring.repository.NotificationRepository;
//
//@Service
//public class NotificationService {
//
//    private final NotificationRepository notificationRepository;
//    private final NotificationChannelRepository channelRepository;
//    private final EmailSenderService emailSenderService;
//
//    public NotificationService(NotificationRepository notificationRepository,
//                               NotificationChannelRepository channelRepository,
//                               EmailSenderService emailSenderService) {
//        this.notificationRepository = notificationRepository;
//        this.channelRepository = channelRepository;
//        this.emailSenderService = emailSenderService;
//    }
//
//    @Transactional
//    public void sendNotifications(AlertRule rule,
//                                  MonitoredService service,
//                                  String errorMessage) {
//
//        Set<NotificationChannel> channels = rule.getChannels();
//        String message = buildMessage(rule, service, errorMessage);
//
//        for (NotificationChannel channel : channels) {
//
//            if (!channel.isEnabled()) continue;
//
//            boolean success = false;
//
//            try {
//
//                if (channel.getType() == ChannelType.EMAIL) {
//                    sendEmail(channel.getTarget(), message);
//                }
//
//                if (channel.getType() == ChannelType.WEBHOOK) {
//                    sendWebhook(channel.getTarget(), message);
//                }
//
//                success = true;
//
//            } catch (Exception e) {
//                System.out.println(">>> EMAIL FAILED: " + e.getMessage());
//            }
//
//            saveNotification(channel, service, message, success);
//        }
//    }
//
//    private void sendEmail(String to, String message) {
//        emailSenderService.sendEmail(
//                to,
//                "🚨 AI Monitoring Alert",
//                message
//        );
//    }
//
//    private void sendWebhook(String url, String message) {
//        System.out.println("🌐 WEBHOOK → " + url);
//        System.out.println(message);
//    }
//
//    private void saveNotification(NotificationChannel channel,
//                                  MonitoredService service,
//                                  String message,
//                                  boolean success) {
//
//    	Notification notification = Notification.builder()
//    	        .service(service)
//    	        .channelType(channel.getType())
//    	        .target(channel.getTarget())
//    	        .message(message)
//    	        .status(success ? NotificationStatus.SENT : NotificationStatus.FAILED)
//    	        .sentAt(LocalDateTime.now()) // ALWAYS SET
//    	        .retryCount(success ? 0 : 1)
//    	        .lastRetryAt(success ? null : LocalDateTime.now())
//    	        .build();
//
//        notificationRepository.save(notification);
//    }
//
//
//    
//    private String buildMessage(AlertRule rule,
//            MonitoredService service,
//            String errorMessage) {
//
//return String.format("""
//🚨 ALERT TRIGGERED
//
//Rule: %s
//Service: %s
//Status: %s
//Time: %s
//
//Error:
//%s
//""",
//rule.getName(),
//service.getName(),
//service.getStatus(),
//LocalDateTime.now(),
//errorMessage
//);
//}
//    
//}

package com.imran.aimonitoring.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.imran.aimonitoring.entity.*;
import com.imran.aimonitoring.repository.NotificationChannelRepository;
import com.imran.aimonitoring.repository.NotificationRepository;

// ✅ IMPROVEMENT #2  — Replaced System.out.println with proper Logger
// ✅ IMPROVEMENT #12 — Implemented sendWebhook() properly using RestTemplate
//                      Before: only printed to console, users never got webhook alerts

@Service
public class NotificationService {

    // ✅ Proper Logger
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationChannelRepository channelRepository;
    private final EmailSenderService emailSenderService;
    private final RestTemplate restTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationChannelRepository channelRepository,
                               EmailSenderService emailSenderService,
                               RestTemplate restTemplate) {
        this.notificationRepository = notificationRepository;
        this.channelRepository      = channelRepository;
        this.emailSenderService     = emailSenderService;
        this.restTemplate           = restTemplate;
    }

    @Transactional
    public void sendNotifications(AlertRule rule,
                                  MonitoredService service,
                                  String errorMessage) {

        Set<NotificationChannel> channels = rule.getChannels();
        String message = buildMessage(rule, service, errorMessage);

        for (NotificationChannel channel : channels) {

            if (!channel.isEnabled()) continue;

            boolean success = false;

            try {
                if (channel.getType() == ChannelType.EMAIL) {
                    sendEmail(channel.getTarget(), message);
                    success = true;
                }

                if (channel.getType() == ChannelType.WEBHOOK) {
                    sendWebhook(channel.getTarget(), message);
                    success = true;
                }

            } catch (Exception e) {
                // ✅ Use logger instead of System.out.println(">>> EMAIL FAILED:")
                log.error("Notification failed: channel={} type={} error={}",
                        channel.getId(), channel.getType(), e.getMessage());
            }

            saveNotification(channel, service, message, success);
        }
    }

    private void sendEmail(String to, String message) {
        emailSenderService.sendEmail(
                to,
                "AI Monitoring Alert",
                message
        );
    }

    // ✅ IMPROVEMENT #12 — Webhook is now properly implemented
    //    Sends a JSON POST request to the webhook URL.
    //    Compatible with Slack, Discord, Teams, and custom webhooks.
    private void sendWebhook(String url, String message) {

        log.info("Sending webhook to url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // JSON body — compatible with Slack/Discord/Teams incoming webhooks
        String jsonBody = "{\"text\": \""
                + message.replace("\"", "'")
                         .replace("\n", "\\n")
                + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        restTemplate.postForEntity(url, entity, String.class);

        log.info("Webhook sent successfully to url={}", url);
    }

    private void saveNotification(NotificationChannel channel,
                                  MonitoredService service,
                                  String message,
                                  boolean success) {

        Notification notification = Notification.builder()
                .service(service)
                .channelType(channel.getType())
                .target(channel.getTarget())
                .message(message)
                .status(success ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .sentAt(LocalDateTime.now())
                .retryCount(success ? 0 : 1)
                .lastRetryAt(success ? null : LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    private String buildMessage(AlertRule rule,
                                MonitoredService service,
                                String errorMessage) {

        return String.format("""
                ALERT TRIGGERED

                Rule: %s
                Service: %s
                Status: %s
                Time: %s

                Error:
                %s
                """,
                rule.getName(),
                service.getName(),
                service.getStatus(),
                LocalDateTime.now(),
                errorMessage
        );
    }
}