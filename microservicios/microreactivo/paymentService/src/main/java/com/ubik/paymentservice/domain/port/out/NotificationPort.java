package com.ubik.paymentservice.domain.port.out;

import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> sendPaymentApproved(Long userId, Long reservationId, BigDecimal amount);
    Mono<Void> sendPaymentRejected(Long userId, Long reservationId, String reason);
}
