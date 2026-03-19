package com.imran.aimonitoring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imran.aimonitoring.service.EmailSenderService;

@RestController
public class TestController {

    private final EmailSenderService emailSenderService;

    public TestController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @GetMapping("/test-mail")
    public String testMail() {

        emailSenderService.sendEmail(
                "imranahmad9942880@gmail.com",
                "Test mail",
                "SMTP working successfully"
        );

        return "Mail sent";
    }
}