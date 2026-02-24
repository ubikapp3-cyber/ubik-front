package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.infrastructure.service.ConfirmationCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador para estadísticas de códigos de confirmación
 * Útil para dashboards de administración
 */
@RestController
@RequestMapping("/api/admin/reservation-stats")
public class ReservationStatsController {

    private final ConfirmationCodeService confirmationCodeService;

    public ReservationStatsController(ConfirmationCodeService confirmationCodeService) {
        this.confirmationCodeService = confirmationCodeService;
    }

    /**
     * Obtiene estadísticas del día actual
     * GET /api/admin/reservation-stats/today
     */
    @GetMapping("/today")
    public Mono<ConfirmationCodeService.DayStats> getTodayStats() {
        return confirmationCodeService.getTodayStats();
    }
}
