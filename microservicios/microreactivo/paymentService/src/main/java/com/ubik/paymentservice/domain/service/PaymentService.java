package com.ubik.paymentservice.domain.service;

import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.ubik.paymentservice.application.dto.CreatePaymentRequest;
import com.ubik.paymentservice.application.dto.PaymentResponse;
import com.ubik.paymentservice.application.dto.WebhookRequest;
import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.domain.port.in.PaymentUseCase;
import com.ubik.paymentservice.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService implements PaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository  paymentRepository;
    private final MotelMpPort        motelMpPort;
    private final ReservationPort    reservationPort;
    private final NotificationPort   notificationPort;

    @Value("${mercadopago.marketplace-fee-percent:10}")
    private int feePercent;

    @Value("${mercadopago.back-urls.success:http://localhost:4200/payment/success}")
    private String backUrlSuccess;

    @Value("${mercadopago.back-urls.failure:http://localhost:4200/payment/failure}")
    private String backUrlFailure;

    @Value("${mercadopago.back-urls.pending:http://localhost:4200/payment/pending}")
    private String backUrlPending;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    public PaymentService(
            PaymentRepository paymentRepository,
            MotelMpPort motelMpPort,
            ReservationPort reservationPort,
            NotificationPort notificationPort) {
        this.paymentRepository = paymentRepository;
        this.motelMpPort       = motelMpPort;
        this.reservationPort   = reservationPort;
        this.notificationPort  = notificationPort;
    }

    // ─── Crear pago (Checkout Pro Marketplace) ──────────────────────────────────

    @Override
    public Mono<PaymentResponse> createPayment(CreatePaymentRequest request, Long userId) {
        log.info("Iniciando pago para reserva={} motel={} userId={}",
                request.reservationId(), request.motelId(), userId);

        BigDecimal fee = request.amount()
                .multiply(BigDecimal.valueOf(feePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Payment draft = new Payment(
                null, request.reservationId(), userId, request.motelId(),
                request.amount(), request.currency() != null ? request.currency() : "COP",
                PaymentStatus.PENDING, null, null, null, null, fee,
                LocalDateTime.now(), LocalDateTime.now()
        );

        return paymentRepository.save(draft)
                .flatMap(saved ->
                        motelMpPort.getAccessToken(request.motelId())
                                .flatMap(token -> createPreference(saved, token, fee))
                                .flatMap(preference ->
                                        paymentRepository.save(
                                                saved.withPreference(
                                                        preference.getId(),
                                                        preference.getInitPoint(),
                                                        fee
                                                )
                                        )
                                )
                )
                .map(PaymentResponse::from)
                .doOnSuccess(r -> log.info("Preferencia creada preferenceId={}", r.mercadopagoPreferenceId()))
                .doOnError(e -> log.error("Error creando pago reserva={}: {}", request.reservationId(), e.getMessage()));
    }

    private Mono<Preference> createPreference(Payment payment, String accessToken, BigDecimal fee) {
        return Mono.fromCallable(() -> {
            com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Reserva UBIK #" + payment.reservationId())
                    .quantity(1)
                    .unitPrice(payment.amount())
                    .currencyId(payment.currency())
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(backUrlSuccess + "?reservationId=" + payment.reservationId())
                    .failure(backUrlFailure + "?reservationId=" + payment.reservationId())
                    .pending(backUrlPending + "?reservationId=" + payment.reservationId())
                    .build();

            PreferenceRequest preferenceReq = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(payment.reservationId().toString())
                    .marketplaceFee(fee)
                    .notificationUrl(notificationUrl)
                    .build();

            return new PreferenceClient().create(preferenceReq);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ─── Webhook ──────────────────────────────────────────────────────────────

    @Override
    public Mono<Void> processWebhook(WebhookRequest request) {
        if (!"payment".equals(request.type())) {
            return Mono.empty();
        }

        log.info("Webhook recibido: type={} id={}", request.type(), request.dataId());

        return fetchMpPayment(request.dataId())
                .flatMap(mpPayment -> {
                    String externalRef = mpPayment.getExternalReference();
                    String mpStatus    = mpPayment.getStatus();
                    String mpPaymentId = String.valueOf(mpPayment.getId());

                    log.info("MP pago {} status={} reserva={}", mpPaymentId, mpStatus, externalRef);

                    return paymentRepository.findLatestByReservationId(Long.parseLong(externalRef))
                            .flatMap(p -> resolveStatus(p, mpStatus, mpPaymentId));
                })
                .doOnError(e -> log.error("Error procesando webhook: {}", e.getMessage()))
                .then();
    }

    private Mono<com.mercadopago.resources.payment.Payment> fetchMpPayment(String id) {
        return Mono.fromCallable(() ->
                new com.mercadopago.client.payment.PaymentClient()
                        .get(Long.parseLong(id))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> resolveStatus(Payment payment, String mpStatus, String mpPaymentId) {
        return switch (mpStatus) {
            case "approved" -> {
                Payment updated = payment
                        .withMpPaymentId(mpPaymentId)
                        .withStatus(PaymentStatus.APPROVED);

                yield paymentRepository.save(updated)
                        .flatMap(saved ->
                                reservationPort.confirmReservation(saved.reservationId())
                                        .then(notificationPort.sendPaymentApproved(
                                                saved.userId(),
                                                saved.reservationId(),
                                                saved.amount()
                                        ))
                        );
            }
            case "rejected", "cancelled" -> {
                Payment updated = payment
                        .withMpPaymentId(mpPaymentId)
                        .withFailure("Pago " + mpStatus + " por MercadoPago");

                yield paymentRepository.save(updated)
                        .flatMap(saved ->
                                reservationPort.cancelReservation(saved.reservationId())
                                        .then(notificationPort.sendPaymentRejected(
                                                saved.userId(),
                                                saved.reservationId(),
                                                "El pago fue " + mpStatus
                                        ))
                        );
            }
            default -> {
                log.warn("Estado MP no manejado: {}", mpStatus);
                yield Mono.empty();
            }
        };
    }

    // ─── Reembolso ────────────────────────────────────────────────────────────

    @Override
    public Mono<PaymentResponse> refundPayment(Long paymentId) {
        log.info("Iniciando reembolso pago={}", paymentId);

        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Pago no encontrado: " + paymentId)))
                .flatMap(payment -> {
                    if (payment.status() != PaymentStatus.APPROVED) {
                        return Mono.error(
                                new RuntimeException("Solo pagos APPROVED pueden reembolsarse"));
                    }
                    return executeMpRefund(payment.mercadopagoPaymentId())
                            .then(paymentRepository.save(
                                    payment.withStatus(PaymentStatus.REFUNDED)))
                            .flatMap(saved ->
                                    reservationPort.cancelReservation(saved.reservationId())
                                            .thenReturn(saved)
                            );
                })
                .map(PaymentResponse::from)
                .doOnSuccess(r -> log.info("Reembolso exitoso pago={}", paymentId))
                .doOnError(e -> log.error("Error reembolso pago={}: {}", paymentId, e.getMessage()));
    }

    private Mono<Void> executeMpRefund(String mpPaymentId) {
        return Mono.fromRunnable(() -> {
            try {
                new com.mercadopago.client.payment.PaymentClient()
                        .refund(Long.parseLong(mpPaymentId));
            } catch (Exception e) {
                throw new RuntimeException("Error al reembolsar en MP: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // ─── Consultas ────────────────────────────────────────────────────────────

    @Override
    public Mono<PaymentResponse> getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Pago no encontrado: " + paymentId)))
                .map(PaymentResponse::from);
    }

    @Override
    public Flux<PaymentResponse> getPaymentsByReservation(Long reservationId) {
        return paymentRepository.findAllByReservationId(reservationId)
                .map(PaymentResponse::from);
    }

    @Override
    public Flux<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findAllByUserId(userId)
                .map(PaymentResponse::from);
    }
}
