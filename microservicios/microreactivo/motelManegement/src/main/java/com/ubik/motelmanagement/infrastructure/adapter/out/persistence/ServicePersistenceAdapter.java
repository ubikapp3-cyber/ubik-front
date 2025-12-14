package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.Service;
import com.ubik.motelmanagement.domain.port.out.ServiceRepositoryPort;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomServiceEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper.ServiceMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.RoomServiceR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.ServiceR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para Service
 * Implementa el puerto de salida utilizando R2DBC
 * Parte de la arquitectura hexagonal - Adaptador secundario
 */
@Component
public class ServicePersistenceAdapter implements ServiceRepositoryPort {

    private final ServiceR2dbcRepository serviceR2dbcRepository;
    private final RoomServiceR2dbcRepository roomServiceR2dbcRepository;
    private final ServiceMapper serviceMapper;

    public ServicePersistenceAdapter(
            ServiceR2dbcRepository serviceR2dbcRepository,
            RoomServiceR2dbcRepository roomServiceR2dbcRepository,
            ServiceMapper serviceMapper) {
        this.serviceR2dbcRepository = serviceR2dbcRepository;
        this.roomServiceR2dbcRepository = roomServiceR2dbcRepository;
        this.serviceMapper = serviceMapper;
    }

    @Override
    public Mono<Service> save(Service service) {
        return Mono.just(service)
                .map(serviceMapper::toEntity)
                .flatMap(serviceR2dbcRepository::save)
                .map(serviceMapper::toDomain);
    }

    @Override
    public Mono<Service> findById(Long id) {
        return serviceR2dbcRepository.findById(id)
                .map(serviceMapper::toDomain);
    }

    @Override
    public Flux<Service> findAll() {
        return serviceR2dbcRepository.findAll()
                .map(serviceMapper::toDomain);
    }

    @Override
    public Mono<Service> findByName(String name) {
        return serviceR2dbcRepository.findByName(name)
                .map(serviceMapper::toDomain);
    }

    @Override
    public Mono<Service> update(Service service) {
        return Mono.just(service)
                .map(serviceMapper::toEntity)
                .flatMap(serviceR2dbcRepository::save)
                .map(serviceMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return serviceR2dbcRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return serviceR2dbcRepository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return serviceR2dbcRepository.existsByName(name);
    }

    @Override
    public Flux<Long> findServiceIdsByRoomId(Long roomId) {
        return roomServiceR2dbcRepository.findByRoomId(roomId)
                .map(RoomServiceEntity::serviceId);
    }

    @Override
    public Mono<Boolean> existsRoomServiceRelation(Long roomId, Long serviceId) {
        return roomServiceR2dbcRepository.existsByRoomIdAndServiceId(roomId, serviceId);
    }

    @Override
    public Mono<Void> addServiceToRoom(Long roomId, Long serviceId) {
        RoomServiceEntity entity = new RoomServiceEntity(roomId, serviceId);
        return roomServiceR2dbcRepository.save(entity)
                .then();
    }

    @Override
    public Mono<Void> removeServiceFromRoom(Long roomId, Long serviceId) {
        return roomServiceR2dbcRepository.deleteByRoomIdAndServiceId(roomId, serviceId);
    }
}