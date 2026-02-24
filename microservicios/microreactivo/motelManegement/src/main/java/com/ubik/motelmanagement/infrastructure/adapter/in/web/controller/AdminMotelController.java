package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.*;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Controlador REST para operaciones de administración de moteles.
 * TODOS LOS ENDPOINTS REQUIEREN ROL ADMIN (ROLE_ID_ADMIN).
 */
@RestController
@RequestMapping("/api/admin/motels")
public class AdminMotelController {

    private static final Logger log = LoggerFactory.getLogger(AdminMotelController.class);

    private final String roleIdAdmin;

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public AdminMotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            @Value("${app.roles.admin:#{environment['ROLE_ID_ADMIN']}}") String roleIdAdmin) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.roleIdAdmin = roleIdAdmin;
    }

    // =========================================================================
    // ENDPOINTS DE CONSULTA
    // =========================================================================

    /**
     * GET /api/admin/motels/pending
     * Devuelve moteles en estado PENDING o UNDER_REVIEW.
     */
    @GetMapping("/pending")
    public Flux<MotelResponse> getPendingMotels(ServerWebExchange exchange) {
        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} consultando moteles pendientes", adminUserId);

        return motelUseCasePort.getAllMotels()
                .filter(motel ->
                        motel.approvalStatus() == Motel.ApprovalStatus.PENDING ||
                                motel.approvalStatus() == Motel.ApprovalStatus.UNDER_REVIEW)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * GET /api/admin/motels?status=APPROVED
     * Devuelve todos los moteles, opcionalmente filtrados por estado.
     */
    @GetMapping
    public Flux<MotelResponse> getMotelsByStatus(
            @RequestParam(required = false) String status,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} consultando moteles con status: '{}'", adminUserId, status);

        Flux<Motel> motels = motelUseCasePort.getAllMotels();

        if (status != null && !status.isBlank()) {
            try {
                Motel.ApprovalStatus approvalStatus =
                        Motel.ApprovalStatus.valueOf(status.toUpperCase());
                motels = motels.filter(m -> m.approvalStatus() == approvalStatus);
            } catch (IllegalArgumentException e) {
                return Flux.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estado inválido: '" + status + "'. Valores permitidos: PENDING, UNDER_REVIEW, APPROVED, REJECTED"));
            }
        }

        return motels.map(motelDtoMapper::toResponse);
    }

    /**
     * GET /api/admin/motels/{id}
     * Devuelve el detalle de un motel por ID.
     */
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} consultando motel {}", adminUserId, id);

        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * GET /api/admin/motels/statistics
     * Devuelve estadísticas de aprobación.
     */
    @GetMapping("/statistics")
    public Mono<ApprovalStatisticsResponse> getApprovalStatistics(ServerWebExchange exchange) {
        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} consultando estadísticas de aprobación", adminUserId);

        return motelUseCasePort.getAllMotels()
                .collectList()
                .map(motels -> {
                    long pending = motels.stream()
                            .filter(m -> m.approvalStatus() == Motel.ApprovalStatus.PENDING)
                            .count();
                    long underReview = motels.stream()
                            .filter(m -> m.approvalStatus() == Motel.ApprovalStatus.UNDER_REVIEW)
                            .count();
                    long approved = motels.stream()
                            .filter(m -> m.approvalStatus() == Motel.ApprovalStatus.APPROVED)
                            .count();
                    long rejected = motels.stream()
                            .filter(m -> m.approvalStatus() == Motel.ApprovalStatus.REJECTED)
                            .count();
                    long incompleteLegalInfo = motels.stream()
                            .filter(m -> !m.hasCompleteLegalInfo())
                            .count();

                    return new ApprovalStatisticsResponse(
                            (long) motels.size(),
                            pending,
                            underReview,
                            approved,
                            rejected,
                            incompleteLegalInfo
                    );
                });
    }

    // =========================================================================
    // ENDPOINTS DE APROBACIÓN
    // =========================================================================

    /**
     * POST /api/admin/motels/{id}/approve
     * Aprueba un motel. Requiere información legal completa.
     */
    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> approveMotel(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApproveMotelRequest request,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} aprobando motel {}", adminUserId, id);

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    if (!motel.hasCompleteLegalInfo()) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "El motel no tiene información legal completa. No se puede aprobar."));
                    }
                    if (motel.approvalStatus() == Motel.ApprovalStatus.APPROVED) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El motel ya está aprobado"));
                    }

                    Motel approvedMotel = motel.approve(adminUserId);

                    return motelUseCasePort.updateMotel(id, approvedMotel)
                            .map(updated -> new ApprovalOperationResponse(
                                    updated.id(),
                                    updated.name(),
                                    motel.approvalStatus().name(),
                                    updated.approvalStatus().name(),
                                    "Motel aprobado exitosamente",
                                    LocalDateTime.now(),
                                    adminUserId
                            ));
                });
    }

    /**
     * POST /api/admin/motels/{id}/reject
     * Rechaza un motel con una razón obligatoria.
     */
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> rejectMotel(
            @PathVariable Long id,
            @Valid @RequestBody RejectMotelRequest request,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} rechazando motel {} — razón: '{}'", adminUserId, id, request.reason());

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    if (motel.approvalStatus() == Motel.ApprovalStatus.REJECTED) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El motel ya está rechazado"));
                    }

                    Motel rejectedMotel = motel.reject(adminUserId, request.reason());

                    return motelUseCasePort.updateMotel(id, rejectedMotel)
                            .map(updated -> new ApprovalOperationResponse(
                                    updated.id(),
                                    updated.name(),
                                    motel.approvalStatus().name(),
                                    updated.approvalStatus().name(),
                                    "Motel rechazado: " + request.reason(),
                                    LocalDateTime.now(),
                                    adminUserId
                            ));
                });
    }

    /**
     * PATCH /api/admin/motels/{id}/review
     * Pone un motel en estado UNDER_REVIEW.
     */
    @PatchMapping("/{id}/review")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> putMotelUnderReview(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdmin(exchange);
        log.info("Admin {} poniendo motel {} en revisión", adminUserId, id);

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    if (motel.approvalStatus() == Motel.ApprovalStatus.UNDER_REVIEW) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El motel ya está en revisión"));
                    }

                    Motel reviewMotel = motel.putUnderReview();

                    return motelUseCasePort.updateMotel(id, reviewMotel)
                            .map(updated -> new ApprovalOperationResponse(
                                    updated.id(),
                                    updated.name(),
                                    motel.approvalStatus().name(),
                                    updated.approvalStatus().name(),
                                    "Motel puesto en revisión",
                                    LocalDateTime.now(),
                                    adminUserId
                            ));
                });
    }

    // =========================================================================
    // HELPER — validación de rol admin
    // =========================================================================

    /**
     * Valida que el request venga de un admin y retorna su userId.
     * Lanza 401 si faltan headers, 403 si el rol no es admin.
     */
    private Long validateAdmin(ServerWebExchange exchange) {
        String role        = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String username    = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        if (role == null || username == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado");
        }

        if (!roleIdAdmin.equals(role)) {
            log.warn("Acceso denegado a endpoint admin — username: '{}', role recibido: '{}'",
                    username, role);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso denegado: solo administradores pueden realizar esta operación");
        }

        // X-User-Id viene del JWT via gateway; es seguro usarlo aquí
        // porque ya pasó la validación de rol admin
        try {
            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Header X-User-Id inválido: " + userIdHeader);
        }
    }
}

record ApprovalStatisticsResponse(
        Long totalMotels,
        Long pendingApproval,
        Long underReview,
        Long approved,
        Long rejected,
        Long withIncompleteLegalInfo
) {}