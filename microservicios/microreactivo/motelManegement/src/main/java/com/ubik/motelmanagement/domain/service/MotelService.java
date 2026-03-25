package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.NotificationPort;
import com.ubik.motelmanagement.domain.port.out.UserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Motel
 * Contiene la lógica de negocio
 */
@Service
public class MotelService implements MotelUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(MotelService.class);

    private final MotelRepositoryPort motelRepositoryPort;
    private final NotificationPort notificationPort;
    private final UserPort userPort;
    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public MotelService(
            NotificationPort notificationPort, 
            UserPort userPort, 
            MotelRepositoryPort motelRepositoryPort,
            DatabaseClient databaseClient,
            TransactionalOperator transactionalOperator
    ) {
        this.notificationPort = notificationPort;
        this.userPort = userPort;
        this.motelRepositoryPort = motelRepositoryPort;
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Motel> createMotel(Motel motel) {

        log.info("Creando motel: {}", motel.name());

        return transactionalOperator.transactional(
            setClientTimeInSession()
                .then(validateMotel(motel))
                .then(motelRepositoryPort.save(motel))
        )

                .flatMap(savedMotel ->

                        userPort.getUserById(savedMotel.propertyId())

                                .flatMap(user ->

                                        notificationPort.sendMotelCreationNotification(
                                                        user.email(),
                                                        savedMotel.name(),
                                                        savedMotel.city(),
                                                        savedMotel.address(),
                                                        savedMotel.phoneNumber(),
                                                        savedMotel.rnt()
                                                )
                                                // Si falla el envío, no romper creación
                                                .onErrorResume(error -> {
                                                    log.error("Error enviando notificación: {}", error.getMessage());
                                                    return Mono.empty();
                                                })
                                                .thenReturn(savedMotel)
                                )

                                // Si no encuentra usuario, no romper creación
                                .defaultIfEmpty(savedMotel)
                )

                .doOnSuccess(saved ->
                        log.info("Motel creado con ID: {}", saved.id())
                );
    }

    @Override
    public Mono<Motel> getMotelById(Long id) {
        log.debug("Obteniendo motel por ID: {}", id);
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)));
    }

    @Override
    public Flux<Motel> getAllMotels() {
        log.debug("Obteniendo todos los moteles");
        return motelRepositoryPort.findAll();
    }

    @Override
    public Flux<Motel> getMotelsByCity(String city) {
        log.debug("Obteniendo moteles por ciudad: {}", city);
        return motelRepositoryPort.findByCity(city);
    }

    @Override
    public Flux<Motel> getMotelsByPropertyId(Long propertyId) {
        log.info("🔍 MotelService.getMotelsByPropertyId({})", propertyId);
        
        return motelRepositoryPort.findByPropertyId(propertyId)
                .doOnSubscribe(subscription -> log.debug("Iniciando búsqueda en repositorio..."))
                .doOnNext(motel -> log.info("  ✓ Motel del servicio: id={}, name='{}', propertyId={}", 
                        motel.id(), motel.name(), motel.propertyId()))
                .doOnComplete(() -> log.info("  ✓ Servicio completó búsqueda para propertyId: {}", propertyId))
                .doOnError(error -> log.error("  ✗ Error en servicio para propertyId {}: {}", 
                        propertyId, error.getMessage(), error));
    }

    @Override
    public Mono<Motel> updateMotel(Long id, Motel motel) {
        log.info("Actualizando motel ID: {}", id);
        return transactionalOperator.transactional(
            motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)))
                .flatMap(existingMotel -> {
                    ApprovalStatus targetApprovalStatus = motel.approvalStatus() != null ? motel.approvalStatus() : existingMotel.approvalStatus();
                    LocalDateTime targetApprovalDate = motel.approvalDate() != null ? motel.approvalDate() : existingMotel.approvalDate();
                    Long targetApprovedByUserId = motel.approvedByUserId() != null ? motel.approvedByUserId() : existingMotel.approvedByUserId();
                    String targetRejectionReason = motel.rejectionReason() != null ? motel.rejectionReason() : existingMotel.rejectionReason();

                    // Si no cambia el estado a APPROVED, mantenemos fecha/usuario aprobador actuales.
                    // Si un admin está aprobando desde /admin/motels/{id}/approve, ya trae approvalDate y approvedByUserId.
                    if (motel.approvalStatus() == null || motel.approvalStatus() != ApprovalStatus.APPROVED) {
                        // para un update normal (sin aprobar) no cascamos fecha/usuario a null
                        targetApprovalDate = existingMotel.approvalDate();
                        targetApprovedByUserId = existingMotel.approvedByUserId();
                    }

                    Motel updatedMotel = new Motel(
                            id,
                            motel.name(),
                            motel.address(),
                            motel.phoneNumber(),
                            motel.description(),
                            motel.city(),
                            existingMotel.propertyId(),
                            existingMotel.dateCreated(),
                            motel.imageUrls(),
                            motel.latitude(),
                            motel.longitude(),
                            targetApprovalStatus,
                            targetApprovalDate,
                            targetApprovedByUserId,
                            targetRejectionReason,
                            motel.rues(),
                            motel.rnt(),
                            motel.ownerDocumentType(),
                            motel.ownerDocumentNumber(),
                            motel.ownerFullName(),
                            motel.legalRepresentativeName(),
                            motel.legalDocumentUrl()
                    );
                    return setClientTimeInSession()
                            .then(validateMotel(updatedMotel))
                            .then(motelRepositoryPort.update(updatedMotel));
                })
        );
    }

    private Mono<Void> setClientTimeInSession() {
        return Mono.deferContextual(ctx -> {
            String clientTime = ctx.getOrDefault("X-Client-Time", null);
            if (clientTime != null && !clientTime.isBlank()) {
                return databaseClient.sql("SELECT set_config('ubik.client_time', :time, true)")
                        .bind("time", clientTime)
                        .then();
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteMotel(Long id) {
        log.info("Eliminando motel ID: {}", id);
        return motelRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Motel no encontrado con ID: " + id));
                    }
                    return motelRepositoryPort.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Motel {} eliminado", id));
    }

    /**
     * Validaciones de negocio para un motel
     */
    private Mono<Void> validateMotel(Motel motel) {
        if (motel.name() == null || motel.name().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El nombre del motel es requerido"));
        }
        if (motel.address() == null || motel.address().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La dirección del motel es requerida"));
        }
        if (motel.city() == null || motel.city().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La ciudad del motel es requerida"));
        }
        if (motel.imageUrls() != null && motel.imageUrls().size() > 10) {
            return Mono.error(new IllegalArgumentException("No se pueden agregar más de 10 imágenes"));
        }
        
        // Validar coordenadas geográficas
        if (motel.latitude() != null && (motel.latitude() < -90.0 || motel.latitude() > 90.0)) {
            return Mono.error(new IllegalArgumentException("La latitud debe estar entre -90 y 90"));
        }
        if (motel.longitude() != null && (motel.longitude() < -180.0 || motel.longitude() > 180.0)) {
            return Mono.error(new IllegalArgumentException("La longitud debe estar entre -180 y 180"));
        }
        
        return Mono.empty();
    }
}