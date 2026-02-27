package com.ubik.motelmanagement.infrastructure.adapter.out.notification;

import com.ubik.motelmanagement.domain.port.out.NotificationPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Component
public class NotificationAdapter implements NotificationPort {

    private final WebClient webClient;

    public NotificationAdapter(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://notification-service:8084")
                // .baseUrl("http://host.docker.internal:8084")
                .build();
    }

    @Override
    public Mono<Void> sendReservationConfirmation(String email, String confirmationCode, String checkIn, String checkOut, String roomId, String totalPrice)
    {


        String htmlMessage = """
            <div style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f6f9; padding:40px 20px;">
                <div style="max-width:600px; margin:0 auto; background:#ffffff; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.05);">
                    
                    <h2 style="color:#2c3e50; text-align:center;">
                        🏨 Confirmación de Reserva
                    </h2>

                    <p>Tu reserva fue creada exitosamente.</p>

                    <div style="text-align:center; margin:25px 0;">
                        <span style="
                            display:inline-block;
                            padding:15px 25px;
                            font-size:18px;
                            letter-spacing:2px;
                            background-color:#2c3e50;
                            color:#ffffff;
                            border-radius:6px;
                            font-weight:bold;">
                            %s
                        </span>
                    </div>

                    <h3 style="color:#444;">Detalles de la reserva</h3>

                    <p><strong>Check-in:</strong> %s</p>
                    <p><strong>Check-out:</strong> %s</p>
                    <p><strong>Habitación:</strong> #%s</p>
                    <p><strong>Total:</strong> %s COP</p>

                    <hr style="margin:25px 0;" />

                    <p style="font-size:13px; color:#777;">
                        Presenta este código al momento del check-in.
                        Sin él no podrás acceder a la habitación.
                    </p>

                    <p style="font-size:12px; color:#aaa; text-align:center;">
                        Gracias por elegir UBIK
                    </p>
                </div>
            </div>
            """.formatted(
                confirmationCode,
                checkIn,
                checkOut,
                roomId,
                totalPrice
        );

        Map<String, String> body = Map.of(
                "to", email,
                "subject", "🏨 Confirmación de Reserva - UBIK",
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
    public Mono<Void> sendMotelCreationNotification(
            String email,
            String motelName,
            String city,
            String address,
            String phone,
            String rnt
    ) {

        String htmlMessage = """
        <div style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f6f9; padding:40px 20px;">
            <div style="max-width:600px; margin:0 auto; background:#ffffff; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.05);">

                <h2 style="color:#2c3e50; text-align:center;">
                    🏨 Nuevo Establecimiento Registrado
                </h2>

                <p>Tu establecimiento ha sido registrado exitosamente en <strong>UBIK</strong>.</p>

                <h3 style="color:#444; margin-top:25px;">Datos del establecimiento</h3>

                <p><strong>Nombre:</strong> %s</p>
                <p><strong>Ciudad:</strong> %s</p>
                <p><strong>Dirección:</strong> %s</p>
                <p><strong>Teléfono:</strong> %s</p>
                <p><strong>RNT:</strong> %s</p>

                <hr style="margin:25px 0;" />

                <p style="color:#e67e22; font-weight:bold;">
                    ⚠ Estado actual: Pendiente de aprobación por UBIK.
                </p>

                <p style="font-size:13px; color:#777;">
                    Nuestro equipo validará la información suministrada.
                    Recibirás una notificación cuando el establecimiento sea aprobado.
                </p>

                <p style="font-size:12px; color:#aaa; text-align:center;">
                    Gracias por confiar en UBIK
                </p>

            </div>
        </div>
        """.formatted(
                motelName,
                city,
                address,
                phone,
                rnt
        );

        Map<String, String> body = Map.of(
                "to", email,
                "subject", "🏨 Registro de Establecimiento Recibido - UBIK",
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
    public Mono<Void> sendRoomCreationNotification(
            String email,
            String motelName,
            String roomName,
            String roomNumber,
            String price,
            String createdAt
    ) {

        String htmlMessage = """
    <div style="font-family: Arial, Helvetica, sans-serif; background-color:#f4f6f9; padding:40px 20px;">
        <div style="max-width:600px; margin:0 auto; background:#ffffff; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.05);">
            
            <h2 style="color:#2c3e50; text-align:center; margin-bottom:25px;">
                🏨 Nueva habitación registrada
            </h2>

            <p style="font-size:15px; color:#333;">
                Se ha creado una nueva habitación en tu motel.
            </p>

            <div style="background:#f8f9fa; padding:20px; border-radius:6px; margin:20px 0;">
                <p><strong>Motel:</strong> %s</p>
                <p><strong>Habitación:</strong> %s</p>
                <p><strong>Número:</strong> %s</p>
                <p><strong>Precio:</strong> $ %s</p>
                <p><strong>Fecha de creación:</strong> %s</p>
            </div>

            <p style="font-size:14px; color:#555;">
                La habitación ya está disponible en el sistema para reservas.
            </p>

            <hr style="margin:30px 0; border:none; border-top:1px solid #eee;" />

            <p style="font-size:12px; color:#999; text-align:center;">
                Este es un mensaje automático del sistema UBIK.
            </p>
        </div>
    </div>
    """.formatted(
                motelName,
                roomName,
                roomNumber,
                price,
                createdAt
        );

        Map<String, String> body = Map.of(
                "to", email,
                "subject", "Nueva habitación creada - " + motelName,
                "message", htmlMessage
        );

        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
