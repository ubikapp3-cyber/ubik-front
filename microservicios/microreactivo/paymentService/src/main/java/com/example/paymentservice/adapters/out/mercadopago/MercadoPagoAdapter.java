package com.example.paymentservice.adapters.out.mercadopago;

import com.acme.payments.application.port.out.MercadoPagoPort;
import com.acme.payments.config.MercadoPagoProperties;
import com.acme.payments.domain.exception.ProviderException;
import com.mercadopago.client.common.RequestOptions;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.payment.PaymentRefundCreateRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);

    private final PreferenceClient preferenceClient;
    private final PaymentClient paymentClient;
    private final MercadoPagoProperties props;

    public MercadoPagoAdapter(PreferenceClient preferenceClient,
                              PaymentClient paymentClient,
                              MercadoPagoProperties props) {
        this.preferenceClient = preferenceClient;
        this.paymentClient = paymentClient;
        this.props = props;
    }

    @Override
    public Mono<CreatedPreference> createPreference(CreatePreferenceRequest req, String idempotencyKey) {
        return Mono.fromCallable(() -> {
                    MPRequestOptions options = buildOptions(idempotencyKey);

                    PreferenceItemRequest item = PreferenceItemRequest.builder()
                            .title(req.title())
                            .quantity(1)
                            .unitPrice(req.unitPrice())
                            .currencyId(req.currency())
                            .build();

                    PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                            .success(req.successUrl())
                            .pending(req.pendingUrl())
                            .failure(req.failureUrl())
                            .build();

                    PreferenceRequest preferenceReq = PreferenceRequest.builder()
                            .items(List.of(item))
                            .backUrls(backUrls)
                            .notificationUrl(req.notificationUrl())
                            .externalReference(req.externalReference())
                            .autoReturn("approved")
                            .build();

                    var pref = preferenceClient.create(preferenceReq, options);
                    log.info("Created MP preference id={}", pref.getId());
                    return new CreatedPreference(pref.getId(), pref.getInitPoint(), pref.getSandboxInitPoint());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(MPApiException.class, ex ->
                        new ProviderException("MP API error creating preference: " + ex.getMessage(), ex))
                .onErrorMap(MPException.class, ex ->
                        new ProviderException("MP SDK error creating preference: " + ex.getMessage(), ex));
    }

    @Override
    public Mono<PaymentSnapshot> getPayment(Long paymentId) {
        return Mono.fromCallable(() -> {
                    var payment = paymentClient.get(paymentId);
                    return new PaymentSnapshot(
                            payment.getId(),
                            payment.getStatus(),
                            payment.getStatusDetail(),
                            payment.getTransactionAmount(),
                            payment.getCurrencyId()
                    );
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(MPApiException.class, ex ->
                        new ProviderException("MP API error fetching payment " + paymentId + ": " + ex.getMessage(), ex))
                .onErrorMap(MPException.class, ex ->
                        new ProviderException("MP SDK error fetching payment " + paymentId + ": " + ex.getMessage(), ex));
    }

    @Override
    public Mono<RefundSnapshot> refundPayment(Long paymentId, BigDecimal amount, String idempotencyKey) {
        return Mono.fromCallable(() -> {
                    MPRequestOptions options = buildOptions(idempotencyKey);
                    PaymentRefundClient refundClient = new PaymentRefundClient();

                    PaymentRefundCreateRequest refundReq = PaymentRefundCreateRequest.builder()
                            .amount(amount)
                            .build();

                    var refund = refundClient.create(paymentId, refundReq, options);
                    return new RefundSnapshot(refund.getId(), refund.getAmount(), refund.getStatus());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(MPApiException.class, ex ->
                        new ProviderException("MP API error refunding payment " + paymentId + ": " + ex.getMessage(), ex))
                .onErrorMap(MPException.class, ex ->
                        new ProviderException("MP SDK error refunding payment " + paymentId + ": " + ex.getMessage(), ex));
    }

    private MPRequestOptions buildOptions(String idempotencyKey) {
        return MPRequestOptions.builder()
                .accessToken(props.accessToken())
                .customHeaders(Map.of("X-Idempotency-Key", idempotencyKey))
                .connectionTimeout(props.connectionTimeoutMs())
                .socketTimeout(props.socketTimeoutMs())
                .build();
    }
}
