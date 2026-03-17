package com.ubik.paymentservice.infrastructure.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de Stripe y WebClient.
 * La inicialización de Stripe.apiKey ya se hace en StripeAdapter,
 * pero esta clase centraliza la config y provee el WebClient builder.
 */
@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.secret-key}")
    private String secretKey;

    /**
     * Inicializa Stripe globalmente al arrancar la aplicación.
     * Aunque StripeAdapter también lo hace, esto garantiza que esté
     * disponible antes de cualquier request.
     */
    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = secretKey;
        log.info("Stripe SDK inicializado correctamente");
    }

    /**
     * WebClient.Builder compartido para los adaptadores de salida HTTP.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
