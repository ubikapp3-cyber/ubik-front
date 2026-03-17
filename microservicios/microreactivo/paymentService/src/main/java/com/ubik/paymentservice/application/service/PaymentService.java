package com.ubik.paymentservice.application.service;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.domain.port.in.PaymentUseCasePort;
import com.ubik.paymentservice.domain.port.out.PaymentRepositoryPort;
import com.ubik.paymentservice.domain.port.out.ReservationConfirmationPort;
import com.ubik.paymentservice.domain.port.out.StripePort;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.CreatePaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación que orquesta la lógica de pagos con Stripe.
 */
@Service
public class PaymentService implements PaymentUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final StripePort stripePort;
    private final PaymentRepositoryPort paymentRepository;
    private final ReservationConfirmationPort reservationConfirmationPort;
    private final InvoiceCreator invoiceCreator;
    private final com.ubik.paymentservice.infrastructure.adapter.out.notification.NotificationAdapter notificationAdapter;
    private final com.ubik.paymentservice.infrastructure.adapter.out.user.UserInfoAdapter userInfoAdapter;
    private final com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.ReservationInfoAdapter reservationInfoAdapter;

    public PaymentService(
            StripePort stripePort,
            PaymentRepositoryPort paymentRepository,
            ReservationConfirmationPort reservationConfirmationPort,
            InvoiceCreator invoiceCreator,
            com.ubik.paymentservice.infrastructure.adapter.out.notification.NotificationAdapter notificationAdapter,
            com.ubik.paymentservice.infrastructure.adapter.out.user.UserInfoAdapter userInfoAdapter,
            com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.ReservationInfoAdapter reservationInfoAdapter
    ) {
        this.stripePort = stripePort;
        this.paymentRepository = paymentRepository;
        this.reservationConfirmationPort = reservationConfirmationPort;
        this.invoiceCreator = invoiceCreator;
        this.notificationAdapter = notificationAdapter;
        this.userInfoAdapter = userInfoAdapter;
        this.reservationInfoAdapter = reservationInfoAdapter;
    }

    @Override
    public Mono<CreatePaymentResponse> createPayment(Long reservationId, Long userId, Long amountCents) {
        log.info("Creando PaymentIntent en Stripe para reserva {} - monto: {} cop", reservationId, amountCents);

        String description = "Reserva #" + reservationId + " - UBIK";

        return stripePort.createPaymentIntent(amountCents, "cop", description)
                .flatMap(clientSecret -> {
                    String paymentIntentId = extractIntentId(clientSecret);

                    Payment payment = Payment.createPending(
                            reservationId,
                            userId,
                            paymentIntentId,
                            amountCents,
                            "cop"
                    );

                    return paymentRepository.save(payment)
                            .map(saved -> {
                                log.info("Pago {} creado en BD para reserva {}", saved.id(), reservationId);
                                return new CreatePaymentResponse(saved.id(), clientSecret);
                            });
                })
                .doOnError(e -> log.error("Error creando PaymentIntent para reserva {}: {}", reservationId, e.getMessage()));
    }

    @Override
    public Mono<Void> handleWebhook(String payload, String stripeSignatureHeader) {
        log.info("Recibiendo evento webhook de Stripe");

        return stripePort.validateWebhookSignature(payload, stripeSignatureHeader)
                .then(stripePort.parseEventType(payload))
                .flatMap(eventType -> {
                    log.info("Tipo de evento Stripe: {}", eventType);

                    return switch (eventType) {
                        case "payment_intent.succeeded" -> handlePaymentSucceeded(payload);
                        case "payment_intent.payment_failed" -> handlePaymentFailed(payload);
                        default -> {
                            log.debug("Evento no manejado: {}", eventType);
                            yield Mono.empty();
                        }
                    };
                });
    }

    @Override
    public Mono<String> getPublishableKey() {
        return stripePort.getPublishableKey();
    }

    @Override
    public Flux<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Flux<Payment> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    // ─── Handlers de eventos Stripe ───────────────────────────────────────────

    private Mono<Void> handlePaymentSucceeded(String payload) {
        return stripePort.parsePaymentIntentId(payload)
                .flatMap(intentId -> {
                    log.info("Pago exitoso para PaymentIntent: {}", intentId);
                    return paymentRepository.updateStatus(intentId, PaymentStatus.SUCCEEDED, null)
                            .flatMap(payment -> {
                                log.info("Pago {} actualizado a SUCCEEDED. Confirmando reserva {}",
                                        payment.id(), payment.reservationId());
                                
                                return reservationConfirmationPort.confirmReservation(payment.reservationId())
                                        .then(generateAndSendInvoice(payment))
                                        .onErrorResume(e -> {
                                            // No fallar el webhook si la confirmación o factura fallan —
                                            // el pago ya está registrado como exitoso
                                            log.error("Error post-pago (confirmación/factura) para reserva {}: {}",
                                                    payment.reservationId(), e.getMessage());
                                            return Mono.empty();
                                        })
                                        .then(generateAndSendInvoice(payment));
                            });
                });
    }

    private Mono<Void> generateAndSendInvoice(Payment payment) {
        return Mono.zip(
                userInfoAdapter.getUserInfo(payment.userId()),
                reservationInfoAdapter.getReservationInfo(payment.reservationId())
        ).flatMap(tuple -> {
            var user = tuple.getT1();
            var res = tuple.getT2();

            String servicesDetail = String.format("Reserva Habitación #%d\nDel: %s\nAl: %s",
                    res.roomId(), res.checkInDate(), res.checkOutDate());

            byte[] pdf = invoiceCreator.generateInvoice(
                    payment.id().toString(),
                    user.username(),
                    user.email(),
                    user.phoneNumber(),
                    servicesDetail,
                    payment.amountCents() / 100.0
            );

            String subject = "Factura de Compra - Reserva #" + payment.reservationId();
            String msg = "<p>Hola " + user.username() + ",</p><p>Adjunto encontrarás la factura de tu reserva.</p><p>¡Gracias por elegir UBIK!</p>";

            return notificationAdapter.sendInvoiceEmail(user.email(), subject, msg, pdf, "Factura_" + payment.id() + ".pdf");
        }).onErrorResume(e -> {
            log.error("Error generando o enviando la factura para el pago {}: {}", payment.id(), e.getMessage());
            return Mono.empty();
        });
    }

    private Mono<Void> handlePaymentFailed(String payload) {
        return Mono.zip(
                stripePort.parsePaymentIntentId(payload),
                stripePort.parseFailureMessage(payload).defaultIfEmpty("Error desconocido")
        ).flatMap(tuple -> {
            String intentId = tuple.getT1();
            String failureMessage = tuple.getT2();
            log.warn("Pago fallido para PaymentIntent {}: {}", intentId, failureMessage);
            return paymentRepository.updateStatus(intentId, PaymentStatus.FAILED, failureMessage).then();
        });
    }

    /**
     * Extrae el ID del PaymentIntent del clientSecret.
     * Formato del clientSecret: "pi_xxxxx_secret_yyyyy" → devuelve "pi_xxxxx"
     */
    private String extractIntentId(String clientSecret) {
        return clientSecret.split("_secret_")[0];
    }
}
