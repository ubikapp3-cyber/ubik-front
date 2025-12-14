package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Service;
import com.ubik.motelmanagement.domain.port.in.ServiceUseCasePort;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.ServiceRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Service
 * Contiene la lógica de negocio para gestión de servicios
 */
@org.springframework.stereotype.Service
public class ServiceService implements ServiceUseCasePort {

    private final ServiceRepositoryPort serviceRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;

    public ServiceService(ServiceRepositoryPort serviceRepositoryPort, RoomRepositoryPort roomRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
    }

    @Override
    public Mono<Service> createService(Service service) {
        return validateService(service)
                .then(serviceRepositoryPort.existsByName(service.name()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Ya existe un servicio con el nombre: " + service.name()));
                    }
                    return serviceRepositoryPort.save(service);
                });
    }

    @Override
    public Mono<Service> getServiceById(Long id) {
        return serviceRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Servicio no encontrado con ID: " + id)));
    }

    @Override
    public Flux<Service> getAllServices() {
        return serviceRepositoryPort.findAll();
    }

    @Override
    public Mono<Service> getServiceByName(String name) {
        return serviceRepositoryPort.findByName(name)
                .switchIfEmpty(Mono.error(new RuntimeException("Servicio no encontrado con nombre: " + name)));
    }

    @Override
    public Mono<Service> updateService(Long id, Service service) {
        return serviceRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Servicio no encontrado con ID: " + id)))
                .flatMap(existingService -> {
                    // Verificar si el nuevo nombre ya existe (excepto si es el mismo servicio)
                    if (!existingService.name().equals(service.name())) {
                        return serviceRepositoryPort.existsByName(service.name())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new IllegalArgumentException("Ya existe un servicio con el nombre: " + service.name()));
                                    }
                                    Service updatedService = new Service(
                                            id,
                                            service.name(),
                                            service.description(),
                                            service.icon(),
                                            existingService.createdAt() // Mantener la fecha de creación original
                                    );
                                    return validateService(updatedService)
                                            .then(serviceRepositoryPort.update(updatedService));
                                });
                    } else {
                        Service updatedService = new Service(
                                id,
                                service.name(),
                                service.description(),
                                service.icon(),
                                existingService.createdAt()
                        );
                        return validateService(updatedService)
                                .then(serviceRepositoryPort.update(updatedService));
                    }
                });
    }

    @Override
    public Mono<Void> deleteService(Long id) {
        return serviceRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Servicio no encontrado con ID: " + id));
                    }
                    return serviceRepositoryPort.deleteById(id);
                });
    }

    @Override
    public Flux<Long> getServiceIdsByRoomId(Long roomId) {
        return serviceRepositoryPort.findServiceIdsByRoomId(roomId);
    }

    @Override
    public Mono<Void> addServiceToRoom(Long roomId, Long serviceId) {
        // Validar que la habitación existe
        return roomRepositoryPort.existsById(roomId)
                .flatMap(roomExists -> {
                    if (!roomExists) {
                        return Mono.error(new RuntimeException("Habitación no encontrada con ID: " + roomId));
                    }
                    // Validar que el servicio existe
                    return serviceRepositoryPort.existsById(serviceId);
                })
                .flatMap(serviceExists -> {
                    if (!serviceExists) {
                        return Mono.error(new RuntimeException("Servicio no encontrado con ID: " + serviceId));
                    }
                    // Verificar si la relación ya existe
                    return serviceRepositoryPort.existsRoomServiceRelation(roomId, serviceId);
                })
                .flatMap(relationExists -> {
                    if (relationExists) {
                        return Mono.error(new IllegalArgumentException(
                                "El servicio con ID " + serviceId + " ya está asociado a la habitación con ID " + roomId));
                    }
                    // Si todo está bien, crear la relación
                    return serviceRepositoryPort.addServiceToRoom(roomId, serviceId);
                });
    }

    @Override
    public Mono<Void> removeServiceFromRoom(Long roomId, Long serviceId) {
        return serviceRepositoryPort.removeServiceFromRoom(roomId, serviceId);
    }

    /**
     * Validaciones de negocio para un servicio
     */
    private Mono<Void> validateService(Service service) {
        if (service.name() == null || service.name().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El nombre del servicio es requerido"));
        }
        if (service.name().length() > 50) {
            return Mono.error(new IllegalArgumentException("El nombre del servicio no puede exceder 50 caracteres"));
        }
        if (service.description() != null && service.description().length() > 255) {
            return Mono.error(new IllegalArgumentException("La descripción no puede exceder 255 caracteres"));
        }
        if (service.icon() != null && service.icon().length() > 50) {
            return Mono.error(new IllegalArgumentException("El icono no puede exceder 50 caracteres"));
        }
        return Mono.empty();
    }
}