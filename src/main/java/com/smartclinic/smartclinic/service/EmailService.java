package com.smartclinic.smartclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simulates sending an email. There is no real mail server/SMTP
 * integration here - every "send" is a structured log line, which is
 * exactly what's asked for in this phase. NotificationService is the
 * layer that decides *what* message to send and to whom; this class
 * only knows how to "deliver" (log) a subject + body to a recipient
 * address, so swapping in a real mail provider later (e.g.
 * JavaMailSender) would mean changing this one class, not every call
 * site that currently calls NotificationService.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * "Sends" an email by logging it. Always succeeds in this simulated
     * version - a real implementation would return false / throw on
     * SMTP failure, which is why this still returns a boolean rather
     * than being void, so call sites don't need to change shape once
     * real delivery is wired in later.
     */
    public boolean sendEmail(String recipientEmail, String subject, String body) {
        log.info("Email sent to {}: subject=\"{}\", body=\"{}\"", recipientEmail, subject, body);
        System.out.println("Email sent to " + recipientEmail + ": " + body);
        return true;
    }
}
