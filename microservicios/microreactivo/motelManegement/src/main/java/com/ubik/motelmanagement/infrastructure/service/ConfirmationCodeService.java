package com.ubik.motelmanagement.infrastructure.service;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.ReservationCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * Características:
 * - Formato: YYMMDD-#### (Ejemplo: 260216-0001)
 * - Contador atómico por día en PostgreSQL
 * - Sin necesidad de Redis
 * - Escalable hasta 9,999 reservas/día
 * - Reseteo automático cada día
 *
 * Ejemplos de códigos generados:
 * - 260216-0001 (Primera reserva del 16 Feb 2026)
 * - 260216-0002 (Segunda reserva del mismo día)
 * - 260217-0001 (Primera reserva del 17 Feb 2026)
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
     * Genera código único con formato YYMMDD-####
     *
     * El incremento es atómico gracias a la query SQL con UPSERT
     *
     * @return Mono<String> con el código generado
     * @throws IllegalStateException si se superan 9,999 reservas en el día
     */
    public Mono<String> generateCode() {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DATE_FORMAT);

        log.debug("🔢 Generando código para fecha: {}", today);

        return counterRepository.incrementAndGet(today)
                .flatMap(sequence -> {
                    // Validar límite diario
                    if (sequence > MAX_RESERVATIONS_PER_DAY) {
                        log.error("Límite diario excedido: {} reservas en {}", sequence, today);
                        return Mono.error(new IllegalStateException(
                                "Se ha alcanzado el límite diario de reservas (9,999). " +
                                        "Por favor intente mañana o contacte a soporte."
                        ));
                    }

                    // Generar código con formato YYMMDD-####
                    String code = String.format("%s-%04d", datePrefix, sequence);
                    log.info("✅ Código generado: {} (secuencia #{} del día)", code, sequence);

                    return Mono.just(code);
                })
                .doOnError(error -> log.error("Error generando código: {}", error.getMessage()));
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