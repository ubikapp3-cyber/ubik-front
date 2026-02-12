package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.Motel;
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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adaptador de persistencia para Motel con soporte de imágenes
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
                        .then(Mono.just(savedEntity)))
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
                .doOnNext(entity -> log.debug("Motel: id={}, name={}", entity.id(), entity.name()))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByCity(String city) {
        log.debug("Buscando moteles en ciudad: {}", city);
        return motelR2dbcRepository.findByCity(city)
                .doOnNext(entity -> log.debug("Motel en {}: {}", city, entity.name()))
                .flatMap(this::loadMotelWithImages);
    }

    @Override
    public Flux<Motel> findByPropertyId(Long propertyId) {
        log.info("🔍 MotelPersistenceAdapter.findByPropertyId({})", propertyId);

        return motelR2dbcRepository.findByPropertyId(propertyId)
                .doOnSubscribe(subscription -> log.debug("Ejecutando query en BD para propertyId: {}", propertyId))
                .doOnNext(entity -> log.info("  ✓ Entity encontrada en BD: id={}, name='{}', propertyId={}",
                        entity.id(), entity.name(), entity.propertyId()))
                .doOnComplete(() -> log.info("  ✓ Query completada para propertyId: {}", propertyId))
                .doOnError(error -> log.error("  ✗ Error en query para propertyId {}: {}",
                        propertyId, error.getMessage(), error))
                .flatMap(entity -> {
                    log.debug("Cargando imágenes para motel ID: {}", entity.id());
                    return loadMotelWithImages(entity);
                });
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
                                .then(Mono.just(savedEntity))
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
     * Carga un motel con sus imágenes
     */
    private Mono<Motel> loadMotelWithImages(com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity entity) {
        return motelImageRepository.findByMotelIdOrderByOrderIndexAsc(entity.id().intValue())
                .map(MotelImageEntity::imageUrl)
                .collectList()
                .map(imageUrls -> {
                    Motel motel = motelMapper.toDomain(entity, imageUrls);
                    log.debug("Motel cargado con {} imágenes: {}", imageUrls.size(), motel.name());
                    return motel;
                });
    }

    /**
     * Guarda las imágenes de un motel
     */
    private Mono<Void> saveImages(Long motelId, java.util.List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            log.debug("No hay imágenes para guardar en motel ID: {}", motelId);
            return Mono.empty();
        }

        log.debug("Guardando {} imágenes para motel ID: {}", imageUrls.size(), motelId);
        
        AtomicInteger order = new AtomicInteger(1);
        return Flux.fromIterable(imageUrls)
                .map(url -> new MotelImageEntity(
                        null,
                        motelId.intValue(),
                        url,
                        order.getAndIncrement()
                ))
                .flatMap(motelImageRepository::save)
                .then()
                .doOnSuccess(v -> log.debug("Imágenes guardadas para motel ID: {}", motelId));
    }
}