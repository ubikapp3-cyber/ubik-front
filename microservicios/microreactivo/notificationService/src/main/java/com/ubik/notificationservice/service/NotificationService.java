package com.ubik.notificationservice.service;

import com.ubik.notificationservice.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotification(NotificationRequest request) {
        // Mock inicial: solo log
        log.info("📨 Enviando notificación");
        log.info("➡️ To: {}", request.getTo());
        log.info("📌 Subject: {}", request.getSubject());
        log.info("📝 Message: {}", request.getMessage());

    }

    public void sendEmail(NotificationRequest dto){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getTo());
        message.setSubject(dto.getSubject());
        message.setText(dto.getMessage());
        message.setFrom("ubikApp3@gmail.com");

        mailSender.send(message);
    }
}
