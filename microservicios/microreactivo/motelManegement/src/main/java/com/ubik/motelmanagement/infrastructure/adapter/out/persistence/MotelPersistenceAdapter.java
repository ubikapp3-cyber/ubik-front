package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper.MotelMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelImageR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de persistencia para Motel con soporte de imágenes
 */
@Component
public class MotelPersistenceAdapter implements MotelRepositoryPort {

    private final MotelR2dbcRepository motelR2dbcRepository;
    private final MotelImageR2dbcRepository motelImageRepository;
    private final MotelMapper motelMapper;

    public MotelPersistenceAdapter(
            MotelR2dbcRepository motelR2dbcRepository,
            MotelImageR2dbcRepository motelImageRepository,
            MotelMapper motelMapper) {
        this.motelR2dbcRepository = motelR2dbcRepository;
        this.motelImageRepository = motelImageRepository;
        this.motelMapper = motelMapper;
    }

    @Override
    public Mono<Motel> save(Motel motel) {
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .flatMap(savedEntity -> saveImages(savedEntity.id(), motel.imageUrls())
                        .then(Mono.just(savedEntity)))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Motel> findById(Long id) {
        return motelR2dbcRepository.findById(id)
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findAll() {
        return motelR2dbcRepository.findAll()
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByCity(String city) {
        return motelR2dbcRepository.findByCity(city)
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByPropertyId(Long propertyId) {
        return motelR2dbcRepository.findByPropertyId(propertyId)
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Motel> update(Motel motel) {
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .flatMap(savedEntity ->
                        motelImageRepository.deleteByMotelId(savedEntity.id().intValue())
                                .then(saveImages(savedEntity.id(), motel.imageUrls()))
                                .then(Mono.just(savedEntity))
                )
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return motelImageRepository.deleteByMotelId(id.intValue())
                .then(motelR2dbcRepository.deleteById(id));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return motelR2dbcRepository.existsById(id);
    }

    /**
     * Carga un motel con sus imágenes
     */
    private Mono<Motel> loadMotelWithImages(com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity entity) {
        return motelImageRepository.findByMotelIdOrderByOrderIndexAsc(entity.id().intValue())
                .map(MotelImageEntity::imageUrl)
                .collectList()
                .map(imageUrls -> motelMapper.toDomain(entity, imageUrls));
    }

    /**
     * Guarda las imágenes de un motel
     */
    private Mono<Void> saveImages(Long motelId, java.util.List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Mono.empty();
        }

        AtomicInteger order = new AtomicInteger(1);
        return Flux.fromIterable(imageUrls)
                .map(url -> new MotelImageEntity(
                        null,
                        motelId.intValue(),
                        url,
                        order.getAndIncrement()
                ))
                .flatMap(motelImageRepository::save)
                .then();
    }
}