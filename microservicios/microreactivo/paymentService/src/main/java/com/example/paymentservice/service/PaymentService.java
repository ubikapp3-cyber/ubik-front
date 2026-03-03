package com.example.paymentservice.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.example.paymentservice.client.MotelManagementClient;
import com.example.paymentservice.client.NotificationClient;
import com.example.paymentservice.domain.PaymentStatus;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.PaymentEntity;
import com.example.paymentservice.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final MotelManagementClient motelManagementClient;
    private final NotificationClient notificationClient;
    private final OAuthService oAuthService;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.marketplace-fee-percent:10}")
    private double marketplaceFeePercent;

    public PaymentService(
            PaymentRepository paymentRepository,
            MotelManagementClient motelManagementClient,
            NotificationClient notificationClient,
            OAuthService oAuthService) {
        this.paymentRepository = paymentRepository;
        this.motelManagementClient = motelManagementClient;
        this.notificationClient = notificationClient;
        this.oAuthService = oAuthService;
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public Mono<PaymentResponse> createPayment(
            CreatePaymentRequest request,
            Long userId,
            Long motelId) {

        log.info("Creando pago marketplace - reserva:{} motel:{} usuario:{}",
                request.reservationId(), motelId, userId);

        return oAuthService.getValidAccessToken(motelId)
                .flatMap(motelAccessToken -> {
                    double fee = request.amount() * (marketplaceFeePercent / 100.0);
                    String currency = request.currency() != null ? request.currency() : "COP";

                    return Mono.fromCallable(() -> {
                                MercadoPagoConfig.setAccessToken(motelAccessToken);
                                PreferenceClient client = new PreferenceClient();

                                PreferenceItemRequest item = PreferenceItemRequest.builder()
                                        .title("Reserva UBIK #" + request.reservationId())
                                        .quantity(1)
                                        .unitPrice(BigDecimal.valueOf(request.amount()))
                                        .currencyId(currency)
                                        .build();

                                PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                                        .success("https://ubik.app/payment/success")
                                        .failure("https://ubik.app/payment/failure")
                                        .pending("https://ubik.app/payment/pending")
                                        .build();

                                PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                                        .items(List.of(item))
                                        .backUrls(backUrls)
                                        .autoReturn("approved")
                                        .externalReference(request.reservationId().toString())
                                        .marketplaceFee(BigDecimal.valueOf(fee))
                                        .build();

                                return client.create(preferenceRequest);
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(preference -> {
                                PaymentEntity entity = new PaymentEntity(
                                        null,
                                        request.reservationId(),
                                        userId,
                                        request.amount(),
                                        currency,
                                        PaymentStatus.PENDING.name(),
                                        null,
                                        preference.getId(),
                                        preference.getInitPoint(),
                                        null,
                                        fee,
                                        motelId,
                                        null,
                                        LocalDateTime.now(),
                                        LocalDateTime.now()
                                );
                                return paymentRepository.save(entity);
                            })
                            .map(this::mapToResponse);
                });
    }

    public Mono<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToResponse);
    }

    public Flux<PaymentResponse> getPaymentsByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(this::mapToResponse);
    }

    public Flux<PaymentResponse> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId)
                .map(this::mapToResponse);
    }

    public Mono<Void> processWebhook(Long mpPaymentId) {
        log.info("Procesando webhook para pago MP: {}", mpPaymentId);

        return paymentRepository.findByMercadoPagoPaymentId(mpPaymentId.toString())
                .switchIfEmpty(fetchAndCreateFromMp(mpPaymentId))
                .flatMap(payment -> {
                    if (PaymentStatus.APPROVED.name().equals(payment.status())) {
                        log.info("Pago {} ya estaba aprobado", mpPaymentId);
                        return Mono.empty();
                    }

                    return fetchPaymentFromMp(payment.motelId(), mpPaymentId)
                            .flatMap(mpPayment -> {
                                String newStatus = mapMpStatus(mpPayment.getStatus());
                                log.info("Actualizando pago {} de {} a {}", mpPaymentId, payment.status(), newStatus);

                                PaymentEntity updated = new PaymentEntity(
                                        payment.id(),
                                        payment.reservationId(),
                                        payment.userId(),
                                        payment.amount(),
                                        payment.currency(),
                                        newStatus,
                                        mpPaymentId.toString(),
                                        payment.mercadoPagoPreferenceId(),
                                        payment.initPoint(),
                                        mpPayment.getStatusDetail(),
                                        payment.marketplaceFee(),
                                        payment.motelId(),
                                        mpPayment.getCollectorId() != null ? mpPayment.getCollectorId().toString() : null,
                                        payment.createdAt(),
                                        LocalDateTime.now()
                                );

                                return paymentRepository.save(updated)
                                        .flatMap(saved -> {
                                            if (PaymentStatus.APPROVED.name().equals(newStatus)) {
                                                return motelManagementClient.confirmReservation(saved.reservationId());
                                            } else if (PaymentStatus.REJECTED.name().equals(newStatus)) {
                                                return motelManagementClient.cancelReservation(saved.reservationId());
                                            }
                                            return Mono.empty();
                                        });
                            });
                })
                .then();
    }

    private Mono<PaymentEntity> fetchAndCreateFromMp(Long mpPaymentId) {
        // En caso de que el webhook llegue antes de que hayamos guardado el record local
        // (poco probable pero posible), o si el pago se hizo por fuera de la app
        return Mono.empty(); // Por simplicidad, ignoramos pagos no registrados localmente
    }

    private Mono<Payment> fetchPaymentFromMp(Long motelId, Long mpPaymentId) {
        return oAuthService.getValidAccessToken(motelId)
                .flatMap(token -> Mono.fromCallable(() -> {
                    MercadoPagoConfig.setAccessToken(token);
                    PaymentClient client = new PaymentClient();
                    return client.get(mpPaymentId);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<PaymentResponse> refundPayment(Long id) {
        return paymentRepository.findById(id)
                .flatMap(payment -> {
                    if (!PaymentStatus.APPROVED.name().equals(payment.status())) {
                        return Mono.error(new RuntimeException("Solo se pueden reembolsar pagos aprobados"));
                    }

                    return oAuthService.getValidAccessToken(payment.motelId())
                            .flatMap(token -> Mono.fromCallable(() -> {
                                MercadoPagoConfig.setAccessToken(token);
                                PaymentRefundClient client = new PaymentRefundClient();
                                return client.refund(Long.parseLong(payment.mercadoPagoPaymentId()));
                            }).subscribeOn(Schedulers.boundedElastic()))
                            .flatMap(refund -> {
                                PaymentEntity updated = new PaymentEntity(
                                        payment.id(),
                                        payment.reservationId(),
                                        payment.userId(),
                                        payment.amount(),
                                        payment.currency(),
                                        PaymentStatus.REFUNDED.name(),
                                        payment.mercadoPagoPaymentId(),
                                        payment.mercadoPagoPreferenceId(),
                                        payment.initPoint(),
                                        "Refunded by admin",
                                        payment.marketplaceFee(),
                                        payment.motelId(),
                                        payment.mpCollectorId(),
                                        payment.createdAt(),
                                        LocalDateTime.now()
                                );
                                return paymentRepository.save(updated);
                            })
                            .map(this::mapToResponse);
                });
    }

    private String mapMpStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING.name();
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED.name();
            case "rejected" -> PaymentStatus.REJECTED.name();
            case "cancelled" -> PaymentStatus.CANCELLED.name();
            case "refunded" -> PaymentStatus.REFUNDED.name();
            case "in_process" -> PaymentStatus.PENDING.name();
            default -> PaymentStatus.PENDING.name();
        };
    }

    private PaymentResponse mapToResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.id(),
                entity.reservationId(),
                entity.userId(),
                entity.amount(),
                entity.currency(),
                entity.status(),
                entity.mercadoPagoPaymentId(),
                entity.mercadoPagoPreferenceId(),
                entity.initPoint(),
                entity.failureReason(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}