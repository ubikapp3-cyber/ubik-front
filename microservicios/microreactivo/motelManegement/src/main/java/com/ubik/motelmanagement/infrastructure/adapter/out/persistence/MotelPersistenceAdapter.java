package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.ImageRole;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.model.MotelImage;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper.MotelMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelImageR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelR2dbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de persistencia para Motel con soporte de imágenes (R2DBC)
 *
 */
@Component
public class MotelPersistenceAdapter implements MotelRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(MotelPersistenceAdapter.class);

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
        log.debug("Guardando motel: {}", motel.name());
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .doOnNext(entity -> log.debug("Motel guardado con ID: {}", entity.id()))
                .flatMap(savedEntity -> saveImages(savedEntity.id(), motel.imageUrls())
                        .thenReturn(savedEntity))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Motel> findById(Long id) {
        log.debug("Buscando motel por ID: {}", id);
        return motelR2dbcRepository.findById(id)
                .doOnNext(entity -> log.debug("Motel encontrado: {}", entity.name()))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findAll() {
        log.debug("Buscando todos los moteles");
        return motelR2dbcRepository.findAll()
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByCity(String city) {
        log.debug("Buscando moteles en ciudad: {}", city);
        return motelR2dbcRepository.findByCity(city)
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByPropertyId(Long propertyId) {
        log.info("MotelPersistenceAdapter.findByPropertyId({})", propertyId);

        return motelR2dbcRepository.findByPropertyId(propertyId)
                .doOnNext(entity -> log.info("  ✓ Entity encontrada en BD: id={}, name='{}', propertyId={}",
                        entity.id(), entity.name(), entity.propertyId()))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Motel> update(Motel motel) {
        log.debug("Actualizando motel ID: {}", motel.id());
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .flatMap(savedEntity ->
                        motelImageRepository.deleteByMotelId(savedEntity.id().intValue())
                                .then(saveImages(savedEntity.id(), motel.imageUrls()))
                                .thenReturn(savedEntity)
                )
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Eliminando motel ID: {}", id);
        return motelImageRepository.deleteByMotelId(id.intValue())
                .then(motelR2dbcRepository.deleteById(id))
                .doOnSuccess(v -> log.info("Motel {} eliminado", id));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        log.debug("Verificando existencia de motel ID: {}", id);
        return motelR2dbcRepository.existsById(id);
    }

    /**
     * Carga un motel con sus imágenes (ordenadas)
     */
    private Mono<Motel> loadMotelWithImages(com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity entity) {
        return motelImageRepository.findOrderedByMotelId(entity.id().intValue())
                .map(img -> new MotelImage(
                        img.id(),
                        img.imageUrl(),
                        ImageRole.valueOf(img.role().toUpperCase()),
                        img.orderIndex()
                ))
                .collectList()
                .map(images -> {
                    Motel motel = motelMapper.toDomain(entity, images);
                    log.debug("Motel cargado con {} imágenes: {}", images.size(), motel.name());
                    return motel;
                });
    }

    /**
     * Guarda las imágenes de un motel.
     *
     * Reglas:
     * - PROFILE y COVER: sortOrder puede ser null
     * - GALLERY: si viene null, se asigna incremental
     */
    private Mono<Void> saveImages(Long motelId, List<MotelImage> images) {
        if (images == null || images.isEmpty()) {
            log.debug("No hay imágenes para guardar en motel ID: {}", motelId);
            return Mono.empty();
        }

        log.debug("Guardando {} imágenes para motel ID: {}", images.size(), motelId);

        AtomicInteger fallbackOrder = new AtomicInteger(1);

        return Flux.fromIterable(images)
                .filter(mi -> mi != null && mi.url() != null && !mi.url().isBlank())
                .map(mi -> {
                    Integer orderIndex = mi.sortOrder();

                    if (mi.role() == ImageRole.GALLERY) {
                        // Si no viene sortOrder, asignamos uno incremental
                        if (orderIndex == null) {
                            orderIndex = fallbackOrder.getAndIncrement();
                        }
                    } else {
                        // PROFILE/COVER: dejamos null
                        orderIndex = null;
                    }

                    return new MotelImageEntity(
                            null,
                            motelId.intValue(),
                            mi.url(),
                            orderIndex,
                            mi.role().name()
                    );
                })
                .flatMap(motelImageRepository::save)
                .then()
                .doOnSuccess(v -> log.debug("Imágenes guardadas para motel ID: {}", motelId));
    }
}