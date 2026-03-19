
package com.imran.aimonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// ✅ IMPROVEMENT #5 — Added @EnableAsync
//    This activates Spring's async execution support.
//    Required for @Async on EmailSenderService.sendEmail() to work.
//    Without this annotation, @Async is silently ignored and emails
//    are still sent synchronously (blocking the health check thread).

@SpringBootApplication
@EnableScheduling
@EnableAsync // ✅ NEW — enables background async email sending
public class AiMonitoringPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMonitoringPlatformApplication.class, args);
//		System.out.println(new BCryptPasswordEncoder().encode("admin123"));
    }
}

