package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.infrastructure.service.CloudinaryService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio extendido de Motel con gestión de imágenes en Cloudinary
 */
@Service
public class MotelServiceWithImages {

    private final MotelRepositoryPort motelRepositoryPort;
    private final CloudinaryService cloudinaryService;

    public MotelServiceWithImages(
            MotelRepositoryPort motelRepositoryPort,
            CloudinaryService cloudinaryService) {
        this.motelRepositoryPort = motelRepositoryPort;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Crea un motel y sube sus imágenes a Cloudinary
     */
    public Mono<Motel> createMotelWithImages(Motel motel, Flux<FilePart> imageFiles) {
        return cloudinaryService.uploadMultipleImages(imageFiles, "motels")
                .collectList()
                .flatMap(imageUrls -> {
                    Motel motelWithImages = new Motel(
                            null,
                            motel.name(),
                            motel.address(),
                            motel.phoneNumber(),
                            motel.description(),
                            motel.city(),
                            motel.propertyId(),
                            motel.dateCreated(),
                            imageUrls,
                            motel.latitude(),
                            motel.longitude(),
                            Motel.ApprovalStatus.PENDING,
                            null,
                            null,
                            null,
                            motel.rues(),
                            motel.rnt(),
                            motel.ownerDocumentType(),
                            motel.ownerDocumentNumber(),
                            motel.ownerFullName(),
                            motel.legalRepresentativeName(),
                            motel.legalDocumentUrl()
                    );
                    
                    return motelRepositoryPort.save(motelWithImages);
                });
    }

    /**
     * Actualiza un motel y reemplaza sus imágenes
     */
    public Mono<Motel> updateMotelWithImages(
            Long id,
            Motel motel,
            Flux<FilePart> imageFiles) {
        
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> {
                    if (imageFiles != null) {
                        return deleteExistingImages(existingMotel.imageUrls())
                                .then(cloudinaryService.uploadMultipleImages(imageFiles, "motels")
                                        .collectList())
                                .flatMap(newUrls -> {
                                    Motel updatedMotel = new Motel(
                                            id,
                                            motel.name(),
                                            motel.address(),
                                            motel.phoneNumber(),
                                            motel.description(),
                                            motel.city(),
                                            existingMotel.propertyId(),
                                            existingMotel.dateCreated(),
                                            newUrls,
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
                                    return motelRepositoryPort.update(updatedMotel);
                                });
                    } else {
                        // Actualizar sin cambiar imágenes
                        Motel updatedMotel = new Motel(
                                id,
                                motel.name(),
                                motel.address(),
                                motel.phoneNumber(),
                                motel.description(),
                                motel.city(),
                                existingMotel.propertyId(),
                                existingMotel.dateCreated(),
                                existingMotel.imageUrls(),
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
                        return motelRepositoryPort.update(updatedMotel);
                    }
                });
    }

    /**
     * Agrega imágenes adicionales a un motel existente
     */
    public Mono<Motel> addImagesToMotel(Long motelId, Flux<FilePart> imageFiles) {
        return motelRepositoryPort.findById(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> 
                    cloudinaryService.uploadMultipleImages(imageFiles, "motels")
                            .collectList()
                            .flatMap(newUrls -> {
                                List<String> allUrls = new ArrayList<>(existingMotel.imageUrls());
                                allUrls.addAll(newUrls);
                                
                                Motel updatedMotel = new Motel(
                                        existingMotel.id(),
                                        existingMotel.name(),
                                        existingMotel.address(),
                                        existingMotel.phoneNumber(),
                                        existingMotel.description(),
                                        existingMotel.city(),
                                        existingMotel.propertyId(),
                                        existingMotel.dateCreated(),
                                        allUrls,
                                        existingMotel.latitude(),
                                        existingMotel.longitude(),
                                        existingMotel.approvalStatus(),
                                        existingMotel.approvalDate(),
                                        existingMotel.approvedByUserId(),
                                        existingMotel.rejectionReason(),
                                        existingMotel.rues(),
                                        existingMotel.rnt(),
                                        existingMotel.ownerDocumentType(),
                                        existingMotel.ownerDocumentNumber(),
                                        existingMotel.ownerFullName(),
                                        existingMotel.legalRepresentativeName(),
                                        existingMotel.legalDocumentUrl()
                                );
                                
                                return motelRepositoryPort.update(updatedMotel);
                            })
                );
    }

    /**
     * Elimina imágenes específicas de un motel
     */
    public Mono<Motel> removeImagesFromMotel(Long motelId, List<String> imageUrlsToRemove) {
        return motelRepositoryPort.findById(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> {
                    return Flux.fromIterable(imageUrlsToRemove)
                            .flatMap(url -> {
                                String publicId = cloudinaryService.extractPublicId(url);
                                return publicId != null 
                                        ? cloudinaryService.deleteImage(publicId)
                                        : Mono.empty();
                            })
                            .then(Mono.defer(() -> {
                                List<String> remainingUrls = new ArrayList<>(existingMotel.imageUrls());
                                remainingUrls.removeAll(imageUrlsToRemove);
                                
                                Motel updatedMotel = new Motel(
                                        existingMotel.id(),
                                        existingMotel.name(),
                                        existingMotel.address(),
                                        existingMotel.phoneNumber(),
                                        existingMotel.description(),
                                        existingMotel.city(),
                                        existingMotel.propertyId(),
                                        existingMotel.dateCreated(),
                                        remainingUrls,
                                        existingMotel.latitude(),
                                        existingMotel.longitude(),
                                        existingMotel.approvalStatus(),
                                        existingMotel.approvalDate(),
                                        existingMotel.approvedByUserId(),
                                        existingMotel.rejectionReason(),
                                        existingMotel.rues(),
                                        existingMotel.rnt(),
                                        existingMotel.ownerDocumentType(),
                                        existingMotel.ownerDocumentNumber(),
                                        existingMotel.ownerFullName(),
                                        existingMotel.legalRepresentativeName(),
                                        existingMotel.legalDocumentUrl()
                                );
                                
                                return motelRepositoryPort.update(updatedMotel);
                            }));
                });
    }

    /**
     * Elimina todas las imágenes de Cloudinary
     */
    private Mono<Void> deleteExistingImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Mono.empty();
        }
        
        return Flux.fromIterable(imageUrls)
                .flatMap(url -> {
                    String publicId = cloudinaryService.extractPublicId(url);
                    return publicId != null 
                            ? cloudinaryService.deleteImage(publicId)
                            : Mono.empty();
                })
                .then();
    }
}