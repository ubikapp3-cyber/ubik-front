package com.ubik.usermanagement.infrastructure.adapter.out.notificationAdapter;

import com.ubik.usermanagement.application.port.out.NotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class NotificationAdapter implements NotificationPort {

    private final WebClient webClient;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public NotificationAdapter(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://notification-service:8084")
                //.baseUrl("http://host.docker.internal:8084")
                .build();
    }

    @Override
    public Mono<Void> sendPasswordRecoveryEmail(String email, String username, String token) {

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String htmlMessage = """
        <div style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f6f9; padding:40px 20px;">
            <div style="max-width:600px; margin:0 auto; background:#ffffff; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.05);">
                
                <h2 style="color:#2c3e50; text-align:center; margin-bottom:20px;">
                    🔐 Recuperación de contraseña
                </h2>

                <p style="font-size:15px; color:#333;">
                    Hola, <strong>%s</strong>
                </p>

                <p style="font-size:15px; color:#333;">
                    Hemos recibido una solicitud para restablecer tu contraseña en <strong>UBIK</strong>.
                </p>

                <p style="font-size:15px; color:#333;">
                    Haz clic en el siguiente botón para restablecer tu contraseña:
                </p>

                <div style="text-align:center; margin:30px 0;">
                    <a href="%s" style="
                        display:inline-block;
                        padding:15px 30px;
                        font-size:16px;
                        background-color:#2c3e50;
                        color:#ffffff;
                        border-radius:6px;
                        text-decoration:none;
                        font-weight:bold;">
                        Restablecer contraseña
                    </a>
                </div>

                <p style="font-size:14px; color:#555;">
                    O copia este enlace en tu navegador:<br/>
                    <a href="%s" style="color:#2c3e50;">%s</a>
                </p>

                <p style="font-size:14px; color:#555;">
                    Este token expirará en 1 hora.
                </p>

                <p style="font-size:14px; color:#555;">
                    Si no solicitaste este cambio, puedes ignorar este mensaje.
                </p>

                <hr style="margin:30px 0; border:none; border-top:1px solid #eee;" />

                <p style="font-size:12px; color:#999; text-align:center;">
                    Este es un mensaje automático. Por favor no responder.
                </p>
            </div>
        </div>
        """.formatted(username, resetLink, resetLink, resetLink);

        Map<String, String> body = Map.of(
                "to", email,
                "subject", "Recuperación de contraseña - UBIK",
                "message", htmlMessage
        );


        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> sendRegisterEmail(String email, String username) {
        Map<String, String> body = Map.of(
                "to", email,
                "subject", "🎉 Bienvenido a UBIK",
                "message",
                """
                <div style="font-family: Arial, sans-serif; padding:20px;">
                    <h2 style="color:#2c3e50;">¡BIENVENIDO A UBIK!</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tu registro fue exitoso 🚀</p>
                    <p>Ya puedes comenzar a usar nuestra plataforma.</p>
                    <br/>
                    <p style="color:gray; font-size:12px;">
                        Este es un mensaje automático, por favor no responder.
                    </p>
                </div>
                """.formatted(username)
        );

        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
