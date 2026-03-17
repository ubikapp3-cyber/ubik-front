package com.ubik.motelmanagement.infrastructure.service;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.ReservationCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Servicio robusto para generación de códigos de confirmación únicos.
 *
 * Características:
 * - Formato: YYMMDD-####-AAA (Ejemplo: 260216-0001-X7P)
 * - YYMMDD: Prefijo de fecha
 * - ####: Contador atómico diario en PostgreSQL
 * - AAA: Sufijo aleatorio (UUID truncado) para evitar colisiones en alta concurrencia
 * - Límite: 9,999 reservas/día
 */
@Service
public class ConfirmationCodeService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationCodeService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int MAX_RESERVATIONS_PER_DAY = 9999;

    private final ReservationCounterRepository counterRepository;

    public ConfirmationCodeService(ReservationCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    /**
     * Genera código único robusto con formato YYMMDD-####-AAA
     *
     * @return Mono<String> con el código generado
     */
    public Mono<String> generateCode() {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DATE_FORMAT);
        
        // Generar sufijo aleatorio de 3 caracteres para desempate
        String randomSuffix = UUID.randomUUID().toString().substring(0, 3).toUpperCase();

        log.debug("🔢 Iniciando generación de código para fecha: {}", today);

        return counterRepository.incrementAndGet(today)
                .map(sequence -> {
                    // Validar límite diario (opcional pero recomendado)
                    int safeSequence = (sequence > MAX_RESERVATIONS_PER_DAY) ? 
                                       (sequence % MAX_RESERVATIONS_PER_DAY) : sequence;

                    // Formato final: YYMMDD-0001-X7P
                    String code = String.format("%s-%04d-%s", datePrefix, safeSequence, randomSuffix);
                    log.info("✅ Código único generado: {} (secuencia #{} del día)", code, sequence);

                    return code;
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No se pudo obtener la secuencia del contador")))
                .doOnError(error -> log.error("❌ Error crítico generando código: {}", error.getMessage()));
    }

    /**
     * Obtiene estadísticas del día actual
     * Útil para monitoreo y dashboards
     */
    public Mono<DayStats> getTodayStats() {
        LocalDate today = LocalDate.now();

        return counterRepository.getCurrentCounter(today)
                .defaultIfEmpty(0)
                .map(counter -> new DayStats(
                        today,
                        counter,
                        MAX_RESERVATIONS_PER_DAY,
                        (counter * 100.0) / MAX_RESERVATIONS_PER_DAY
                ));
    }

    /**
     * DTO para estadísticas del día
     */
    public record DayStats(
            LocalDate date,
            int reservationsCreated,
            int dailyLimit,
            double percentageUsed
    ) {
        public boolean isNearLimit() {
            return percentageUsed >= 90.0;
        }

        public int remainingSlots() {
            return dailyLimit - reservationsCreated;
        }
    }
}