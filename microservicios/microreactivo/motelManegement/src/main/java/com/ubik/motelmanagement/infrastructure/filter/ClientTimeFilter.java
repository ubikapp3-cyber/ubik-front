package com.ubik.motelmanagement.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Filtro para capturar la hora del cliente enviada en la cabecera X-Client-Time.
 * Permite sincronizar la lógica de negocio con la zona horaria del usuario (ej. Colombia).
 */
@Component
public class ClientTimeFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(ClientTimeFilter.class);

    public static final String CLIENT_TIME_CONTEXT_KEY = "X-Client-Time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientTime = exchange.getRequest().getHeaders().getFirst("X-Client-Time");

        if (clientTime == null || clientTime.isBlank()) {
            clientTime = exchange.getRequest().getQueryParams().getFirst("client_time");
        }

        if (clientTime != null && !clientTime.isBlank()) {
            final String resolvedTime = clientTime; // effectively final para el lambda
            log.info("Sincronizando tiempo del cliente: {}", resolvedTime);
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(CLIENT_TIME_CONTEXT_KEY, resolvedTime));
        }

        log.warn("No se recibió tiempo del cliente (X-Client-Time o client_time) para la ruta: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
