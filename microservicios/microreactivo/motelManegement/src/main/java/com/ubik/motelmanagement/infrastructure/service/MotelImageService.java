package com.ubik.motelmanagement.infrastructure.service;
import com.ubik.motelmanagement.domain.model.ImageRole;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelImageR2dbcRepository;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.MotelR2dbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
public class MotelImageService {

    private final MotelR2dbcRepository motelRepository;
    private final MotelImageR2dbcRepository motelImageR2dbcRepository;
    private final TransactionalOperator tx;

    public MotelImageService(MotelR2dbcRepository motelRepository,
                             MotelImageR2dbcRepository motelImageR2dbcRepository,
                             TransactionalOperator tx) {
        this.motelRepository = motelRepository;
        this.motelImageR2dbcRepository = motelImageR2dbcRepository;
        this.tx = tx;
    }

    public Mono<Void> setProfileImage(Long motelId, String url) {
        validateUrl(url);

        return requireMotelExists(motelId)
                .then(motelImageR2dbcRepository.deleteByMotelIdAndRole(motelId.intValue(), ImageRole.PROFILE.name()))
                .then(motelImageR2dbcRepository.save(new MotelImageEntity(null, motelId.intValue(), url, null, ImageRole.PROFILE.name())))
                .then()
                .as(tx::transactional);
    }

    public Mono<Void> setCoverImage(Long motelId, String url) {
        validateUrl(url);

        return requireMotelExists(motelId)
                .then(motelImageR2dbcRepository.deleteByMotelIdAndRole(motelId.intValue(), ImageRole.COVER.name()))
                .then(motelImageR2dbcRepository.save(new MotelImageEntity(null, motelId.intValue(), url, null, ImageRole.COVER.name())))
                .then()
                .as(tx::transactional);
    }

    public Mono<Void> addGalleryImage(Long motelId, String url) {
        validateUrl(url);

        return requireMotelExists(motelId)
                .then(motelImageR2dbcRepository.maxGalleryOrder(motelId.intValue()).defaultIfEmpty(0))
                .flatMap(max -> motelImageR2dbcRepository.save(new MotelImageEntity(null, motelId.intValue(), url, max + 1, ImageRole.GALLERY.name())))
                .then()
                .as(tx::transactional);
    }

    public Mono<Void> deleteImage(Long motelId, Long imageId) {
        // En R2dbcRepository<MotelImageEntity, Integer> el id es Integer
        return motelImageR2dbcRepository.deleteById(imageId.intValue())
                .then()
                .as(tx::transactional);
    }

    private Mono<Void> requireMotelExists(Long motelId) {
        return motelRepository.existsById(motelId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new RuntimeException("Motel no existe")));
    }

    private void validateUrl(String url) {
        if (!StringUtils.hasText(url) || url.length() < 8) {
            throw new IllegalArgumentException("URL inválida");
        }
    }
}
