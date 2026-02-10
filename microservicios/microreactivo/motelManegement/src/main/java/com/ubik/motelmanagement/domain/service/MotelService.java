package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Motel
 * Contiene la lógica de negocio
 */
@Service
public class MotelService implements MotelUseCasePort {

    private final MotelRepositoryPort motelRepositoryPort;

    public MotelService(MotelRepositoryPort motelRepositoryPort) {
        this.motelRepositoryPort = motelRepositoryPort;
    }

    @Override
    public Mono<Motel> createMotel(Motel motel) {
        // Validaciones de negocio
        return validateMotel(motel)
                .then(motelRepositoryPort.save(motel));
    }

    @Override
    public Mono<Motel> getMotelById(Long id) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)));
    }

    @Override
    public Flux<Motel> getAllMotels() {
        return motelRepositoryPort.findAll();
    }

    @Override
    public Flux<Motel> getMotelsByCity(String city) {
        return motelRepositoryPort.findByCity(city);
    }

    @Override
    public Flux<Motel> getMotelsByPropertyId(Long propertyId) {
        return motelRepositoryPort.findByPropertyId(propertyId);
    }

    @Override
    public Mono<Motel> updateMotel(Long id, Motel motel) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado con ID: " + id)))
                .flatMap(existingMotel -> {
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
                            existingMotel.approvalStatus(),
                            existingMotel.approvalDate(),
                            existingMotel.approvedByUserId(),
                            existingMotel.rejectionReason(),
                            motel.rues(),
                            motel.rnt(),
                            motel.ownerDocumentType(),
                            motel.ownerDocumentNumber(),
                            motel.ownerFullName(),
                            motel.legalRepresentativeName(),
                            motel.legalDocumentUrl()
                    );
                    return validateMotel(updatedMotel)
                            .then(motelRepositoryPort.update(updatedMotel));
                });
    }

    @Override
    public Mono<Void> deleteMotel(Long id) {
        return motelRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Motel no encontrado con ID: " + id));
                    }
                    return motelRepositoryPort.deleteById(id);
                });
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