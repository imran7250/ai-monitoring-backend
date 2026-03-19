//
//
//package com.imran.aimonitoring.service;
//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailSenderService {
//
//    private final JavaMailSender mailSender;
//
//    public EmailSenderService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendEmail(String to, String subject, String body) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//
//        mailSender.send(message);
//
//        System.out.println("=== REAL EMAIL SENT ===");
//    }
//}

package com.imran.aimonitoring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// ✅ IMPROVEMENT #5 — Email sending is now @Async
//
//    Before: When a service went DOWN, the health check thread sent
//    the email synchronously. If Gmail SMTP took 2-3 seconds, ALL
//    health checks were blocked during that time.
//
//    After: @Async runs sendEmail() in a separate thread from a pool.
//    Health check thread returns immediately and continues checking
//    other services. Email is sent in the background.
//
//    IMPORTANT: You must also add @EnableAsync to
//    AiMonitoringPlatformApplication.java (see that file).

@Service
public class EmailSenderService {

    // ✅ Use proper Logger instead of System.out.println
    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ @Async — runs in background thread, does not block the caller
    @Async
    public void sendEmail(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            // ✅ Use logger instead of System.out.println("=== REAL EMAIL SENT ===")
            log.info("Email sent successfully to={}", to);

        } catch (Exception e) {
            log.error("Failed to send email to={} subject={} error={}", to, subject, e.getMessage());
        }
    }
}
