package com.ubik.paymentservice.infrastructure.adapter.out.notificationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;

    public NotificationClient(
            @Value("${services.notification-service.url}") String notificationServiceUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }

    public Mono<Void> sendEmailWithAttachment(String to, String subject, String message, byte[] attachment, String attachmentName) {
        log.info("Enviando email a {} mediante notification-service", to);

        NotificationRequest request = new NotificationRequest(to, subject, message);
        request.setAttachment(attachment);
        request.setAttachmentName(attachmentName);

        return webClient.post()
                .uri("/notifications/email")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Email enviado exitosamente a {}", to))
                .doOnError(e -> log.error("Error enviando email a {}: {}", to, e.getMessage()));
    }

    public static class NotificationRequest {
        private String to;
        private String subject;
        private String message;
        private byte[] attachment;
        private String attachmentName;

        public NotificationRequest() {}
        public NotificationRequest(String to, String subject, String message) {
            this.to = to;
            this.subject = subject;
            this.message = message;
        }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public byte[] getAttachment() { return attachment; }
        public void setAttachment(byte[] attachment) { this.attachment = attachment; }

        public String getAttachmentName() { return attachmentName; }
        public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    }
}
