package com.ubik.paymentservice.infrastructure.adapter.out.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationAdapter {

    private static final Logger log = LoggerFactory.getLogger(NotificationAdapter.class);
    private final WebClient webClient;

    public NotificationAdapter(@Value("${services.notification-service.url:http://notification-service:8084}") String notificationUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(notificationUrl)
                .build();
    }

    public Mono<Void> sendInvoiceEmail(String toEmail, String subject, String message, byte[] pdfAttachment, String attachmentName) {
        log.info("Enviando correo con factura a {}", toEmail);

        return webClient.post()
                .uri("/notifications/email")
                .bodyValue(new NotificationRequestDto(toEmail, subject, message, pdfAttachment, attachmentName))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Correo de factura enviado a {}", toEmail))
                .doOnError(e -> log.error("Error enviando correo de factura a {}: {}", toEmail, e.getMessage()));
    }

    record NotificationRequestDto(String to, String subject, String message, byte[] attachment, String attachmentName) {}
}
