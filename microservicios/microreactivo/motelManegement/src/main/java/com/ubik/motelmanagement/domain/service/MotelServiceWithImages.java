package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.ImageRole;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.MotelImage;
import com.ubik.motelmanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.NotificationPort;
import com.ubik.motelmanagement.domain.port.out.UserPort;
import com.ubik.motelmanagement.infrastructure.service.CloudinaryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio extendido de Motel con gestión de imágenes en Cloudinary
 *
 * - Las imágenes subidas por estos endpoints se manejan como GALLERY.
 * - PROFILE/COVER se setean por endpoints dedicados (si los agregas luego).
 */
@Service
public class MotelServiceWithImages {

    private final MotelRepositoryPort motelRepositoryPort;
    private final CloudinaryService cloudinaryService;
    private final NotificationPort notificationPort;
    private final UserPort userPort;

    public MotelServiceWithImages(
            MotelRepositoryPort motelRepositoryPort,
            CloudinaryService cloudinaryService, NotificationPort notificationPort, UserPort userPort) {
        this.motelRepositoryPort = motelRepositoryPort;
        this.cloudinaryService = cloudinaryService;
        this.notificationPort = notificationPort;
        this.userPort = userPort;
    }

    /**
     * Crea un motel con imágenes ya subidas a Cloudinary (GALLERY)
     */
    public Mono<Motel> createMotelWithImages(Motel motel) {
        Motel motelWithImages = new Motel(
                null,
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated(),
                motel.imageUrls(), // Ya vienen mapeadas como GALLERY en el DTO Mapper
                motel.latitude(),
                motel.longitude(),
                ApprovalStatus.PENDING,
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

        return motelRepositoryPort.save(motelWithImages)
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
                                                .onErrorResume(error -> Mono.empty())
                                                .thenReturn(savedMotel)
                                )
                                .defaultIfEmpty(savedMotel)
                );
    }

    /**
     * Actualiza un motel y reemplaza sus imágenes de galería (borra en Cloudinary las existentes que ya no estén)
     */
    public Mono<Motel> updateMotelWithImages(
            Long id,
            Motel motel) {

        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> {
                    // Identificar imágenes a borrar en Cloudinary (estaban antes y ya no están)
                    List<MotelImage> imagesToDelete = existingMotel.imageUrls().stream()
                            .filter(oldImg -> motel.imageUrls().stream()
                                    .noneMatch(newImg -> newImg.url().equals(oldImg.url())))
                            .toList();

                    return deleteExistingImages(imagesToDelete)
                            .then(Mono.defer(() -> {
                                Motel updatedMotel = new Motel(
                                        id,
                                        motel.name(),
                                        motel.address(),
                                        motel.phoneNumber(),
                                        motel.description(),
                                        motel.city(),
                                        existingMotel.propertyId(),
                                        existingMotel.dateCreated(),
                                        motel.imageUrls(), // Las nuevas imágenes
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
                            }));
                });
    }

    /**
     * Agrega imágenes adicionales (URLs) a un motel existente (GALLERY)
     */
    public Mono<Motel> addImagesToMotel(Long motelId, List<String> newImageUrls) {
        return motelRepositoryPort.findById(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> {
                    int nextOrder = nextGalleryOrder(existingMotel.imageUrls());
                    List<MotelImage> newGallery = urlsToGallery(newImageUrls, nextOrder);

                    List<MotelImage> all = new ArrayList<>(existingMotel.imageUrls());
                    all.addAll(newGallery);

                    Motel updatedMotel = new Motel(
                            existingMotel.id(),
                            existingMotel.name(),
                            existingMotel.address(),
                            existingMotel.phoneNumber(),
                            existingMotel.description(),
                            existingMotel.city(),
                            existingMotel.propertyId(),
                            existingMotel.dateCreated(),
                            all,
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
                });
    }


    /**
     * Elimina imágenes específicas (por URL) de un motel
     */
    public Mono<Motel> removeImagesFromMotel(Long motelId, List<String> imageUrlsToRemove) {
        return motelRepositoryPort.findById(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel ->
                        Flux.fromIterable(imageUrlsToRemove != null ? imageUrlsToRemove : List.of())
                                .flatMap(url -> {
                                    String publicId = cloudinaryService.extractPublicId(url);
                                    return publicId != null ? cloudinaryService.deleteImage(publicId) : Mono.empty();
                                })
                                .then(Mono.defer(() -> {
                                    List<MotelImage> remaining = new ArrayList<>(existingMotel.imageUrls());
                                    remaining.removeIf(mi -> mi != null && imageUrlsToRemove != null && imageUrlsToRemove.contains(mi.url()));

                                    // Re-normalizar sortOrder de galería (opcional pero recomendado)
                                    remaining = renormalizeGalleryOrder(remaining);

                                    Motel updated = new Motel(
                                            existingMotel.id(),
                                            existingMotel.name(),
                                            existingMotel.address(),
                                            existingMotel.phoneNumber(),
                                            existingMotel.description(),
                                            existingMotel.city(),
                                            existingMotel.propertyId(),
                                            existingMotel.dateCreated(),
                                            remaining,
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

                                    return motelRepositoryPort.update(updated);
                                }))
                );
    }

    // =======================
    // Helpers
    // =======================

    private List<MotelImage> urlsToGallery(List<String> urls, int startOrder) {
        List<MotelImage> out = new ArrayList<>();
        if (urls == null || urls.isEmpty()) return out;

        int order = startOrder;
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            out.add(new MotelImage(null, url, ImageRole.GALLERY, order++));
        }
        return out;
    }

    private int nextGalleryOrder(List<MotelImage> images) {
        int max = 0;
        if (images != null) {
            for (MotelImage mi : images) {
                if (mi != null && mi.role() == ImageRole.GALLERY && mi.sortOrder() != null) {
                    max = Math.max(max, mi.sortOrder());
                }
            }
        }
        return max + 1;
    }

    /**
     * Elimina todas las imágenes existentes en Cloudinary (usa las urls del dominio)
     */
    private Mono<Void> deleteExistingImages(List<MotelImage> images) {
        if (images == null || images.isEmpty()) return Mono.empty();

        return Flux.fromIterable(images)
                .map(mi -> mi != null ? mi.url() : null)
                .filter(url -> url != null && !url.isBlank())
                .flatMap(url -> {
                    String publicId = cloudinaryService.extractPublicId(url);
                    return publicId != null ? cloudinaryService.deleteImage(publicId) : Mono.empty();
                })
                .then();
    }

    /**
     * Reasigna sortOrder de galería a 1..n manteniendo orden actual.
     */
    private List<MotelImage> renormalizeGalleryOrder(List<MotelImage> images) {
        if (images == null || images.isEmpty()) return images;

        List<MotelImage> out = new ArrayList<>();
        int order = 1;

        for (MotelImage mi : images) {
            if (mi == null) continue;

            if (mi.role() == ImageRole.GALLERY) {
                out.add(new MotelImage(mi.id(), mi.url(), mi.role(), order++));
            } else {
                out.add(mi); // PROFILE/COVER no se toca
            }
        }
        return out;
    }
}