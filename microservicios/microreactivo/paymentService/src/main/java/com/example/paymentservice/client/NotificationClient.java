package com.example.paymentservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private final WebClient webClient;

    public NotificationClient(
            @Value("${services.notification.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<Void> sendPaymentApprovedEmail(String email, Long reservationId, Double amount) {
        String html = """
            <div style="font-family: Arial, sans-serif; padding: 30px; background:#f4f6f9;">
              <div style="max-width:600px; margin:0 auto; background:#fff; border-radius:8px; padding:30px;">
                <h2 style="color:#27ae60; text-align:center;">✅ Pago Confirmado</h2>
                <p>Tu pago fue procesado exitosamente.</p>
                <p><strong>Reserva:</strong> #%d</p>
                <p><strong>Total pagado:</strong> $%,.2f COP</p>
                <p style="color:#777; font-size:12px; text-align:center;">Gracias por elegir UBIK</p>
              </div>
            </div>
            """.formatted(reservationId, amount);

        return sendEmail(email, "✅ Pago confirmado - UBIK", html);
    }

    public Mono<Void> sendPaymentRejectedEmail(String email, Long reservationId) {
        String html = """
            <div style="font-family: Arial, sans-serif; padding: 30px; background:#f4f6f9;">
              <div style="max-width:600px; margin:0 auto; background:#fff; border-radius:8px; padding:30px;">
                <h2 style="color:#e74c3c; text-align:center;">❌ Pago Rechazado</h2>
                <p>Tu pago para la reserva <strong>#%d</strong> fue rechazado.</p>
                <p>Por favor intenta nuevamente o usa otro método de pago.</p>
              </div>
            </div>
            """.formatted(reservationId);

        return sendEmail(email, "❌ Pago rechazado - UBIK", html);
    }

    private Mono<Void> sendEmail(String to, String subject, String html) {
        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(Map.of("to", to, "subject", subject, "message", html))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Error enviando email a {}: {}", to, e.getMessage());
                    return Mono.empty();
                });
    }
}