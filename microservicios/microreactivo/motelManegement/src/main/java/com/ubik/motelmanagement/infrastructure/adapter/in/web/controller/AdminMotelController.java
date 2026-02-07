package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Controlador REST para operaciones de administración de moteles
 *
 * TODOS LOS ENDPOINTS REQUIEREN ROL ADMIN (role = 1)
 *
 * Endpoints:
 * - GET    /api/admin/motels/pending        - Lista moteles pendientes de aprobación
 * - GET    /api/admin/motels/{id}           - Obtiene un motel (cualquier estado)
 * - POST   /api/admin/motels/{id}/approve   - Aprueba un motel
 * - POST   /api/admin/motels/{id}/reject    - Rechaza un motel
 * - PATCH  /api/admin/motels/{id}/review    - Pone un motel en revisión
 * - GET    /api/admin/motels/statistics     - Estadísticas de aprobación
 */
@RestController
@RequestMapping("/api/admin/motels")
public class AdminMotelController {

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public AdminMotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * ADMIN - Lista moteles pendientes de aprobación
     * GET /api/admin/motels/pending
     */
    @GetMapping("/pending")
    public Flux<MotelResponse> getPendingMotels(ServerWebExchange exchange) {
        validateAdminRole(exchange);

        return motelUseCasePort.getAllMotels()
                .filter(motel -> motel.approvalStatus() == Motel.ApprovalStatus.PENDING ||
                        motel.approvalStatus() == Motel.ApprovalStatus.UNDER_REVIEW)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * ADMIN - Obtiene todos los moteles filtrados por estado
     * GET /api/admin/motels?status=APPROVED
     */
    @GetMapping
    public Flux<MotelResponse> getMotelsByStatus(
            @RequestParam(required = false) String status,
            ServerWebExchange exchange) {

        validateAdminRole(exchange);

        Flux<Motel> motels = motelUseCasePort.getAllMotels();

        if (status != null && !status.isBlank()) {
            try {
                Motel.ApprovalStatus approvalStatus = Motel.ApprovalStatus.valueOf(status.toUpperCase());
                motels = motels.filter(motel -> motel.approvalStatus() == approvalStatus);
            } catch (IllegalArgumentException e) {
                return Flux.error(new IllegalArgumentException("Estado inválido: " + status));
            }
        }

        return motels.map(motelDtoMapper::toResponse);
    }

    /**
     * ADMIN - Obtiene un motel específico (cualquier estado)
     * GET /api/admin/motels/{id}
     */
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        validateAdminRole(exchange);

        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * ADMIN - Aprueba un motel
     * POST /api/admin/motels/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> approveMotel(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ApproveMotelRequest request,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdminRole(exchange);

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    // Validar que el motel tenga información legal completa
                    if (!motel.hasCompleteLegalInfo()) {
                        return Mono.error(new IllegalArgumentException(
                                "El motel no tiene información legal completa. No se puede aprobar."));
                    }

                    // Validar que el motel no esté ya aprobado
                    if (motel.approvalStatus() == Motel.ApprovalStatus.APPROVED) {
                        return Mono.error(new IllegalArgumentException(
                                "El motel ya está aprobado"));
                    }

                    // Aprobar el motel
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
     * ADMIN - Rechaza un motel
     * POST /api/admin/motels/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> rejectMotel(
            @PathVariable Long id,
            @Valid @RequestBody RejectMotelRequest request,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdminRole(exchange);

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    // Validar que el motel no esté ya rechazado
                    if (motel.approvalStatus() == Motel.ApprovalStatus.REJECTED) {
                        return Mono.error(new IllegalArgumentException(
                                "El motel ya está rechazado"));
                    }

                    // Rechazar el motel
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
     * ADMIN - Pone un motel en revisión
     * PATCH /api/admin/motels/{id}/review
     */
    @PatchMapping("/{id}/review")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ApprovalOperationResponse> putMotelUnderReview(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        Long adminUserId = validateAdminRole(exchange);

        return motelUseCasePort.getMotelById(id)
                .flatMap(motel -> {
                    // Poner en revisión
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

    /**
     * ADMIN - Obtiene estadísticas de aprobación
     * GET /api/admin/motels/statistics
     */
    @GetMapping("/statistics")
    public Mono<ApprovalStatisticsResponse> getApprovalStatistics(ServerWebExchange exchange) {
        validateAdminRole(exchange);

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

    /**
     * Valida que el usuario tenga rol de administrador (role = 1)
     * @return ID del usuario administrador
     * @throws ResponseStatusException si no es admin
     */
    private Long validateAdminRole(ServerWebExchange exchange) {
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        // El gateway debería enviar el ID del usuario, pero si no está, lo extraemos del username
        // En producción, el gateway debería enviar X-User-Id
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (role == null || !role.equals("1")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acceso denegado: Solo administradores pueden realizar esta operación");
        }

        // Retornar el ID del admin (si está disponible)
        return userIdHeader != null ? Long.parseLong(userIdHeader) : 1L;
    }
}

/**
 * DTO para estadísticas de aprobación
 */
record ApprovalStatisticsResponse(
        Long totalMotels,
        Long pendingApproval,
        Long underReview,
        Long approved,
        Long rejected,
        Long withIncompleteLegalInfo
) {
}