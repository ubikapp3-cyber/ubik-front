package com.ubik.notificationservice.service;

import com.ubik.notificationservice.dto.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
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
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.getTo());
        message.setSubject(dto.getSubject());
        message.setText(dto.getMessage());
        message.setFrom("ubikApp3@gmail.com");

        mailSender.send(message);
    }*/
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());

            // 👇 AQUÍ ESTÁ LA CLAVE
            helper.setText(dto.getMessage(), true); // true = HTML

            if (dto.getAttachment() != null && dto.getAttachmentName() != null) {
                helper.addAttachment(dto.getAttachmentName(), new ByteArrayResource(dto.getAttachment()));
            }

            helper.setFrom("ubikApp3@gmail.com");

            mailSender.send(mimeMessage);

            log.info("✅ Correo enviado correctamente a {}", dto.getTo());

        } catch (Exception e) {
            log.error("❌ Error enviando correo", e);
            throw new RuntimeException("Error sending email");
        }
    }
}
