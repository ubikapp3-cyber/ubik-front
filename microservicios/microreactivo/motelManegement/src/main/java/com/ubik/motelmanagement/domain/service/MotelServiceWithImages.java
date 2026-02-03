package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
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
     * 
     * @param motel Motel a crear (sin URLs de imágenes)
     * @param imageFiles Archivos de imágenes
     * @return Motel creado con URLs de Cloudinary
     */
    public Mono<Motel> createMotelWithImages(Motel motel, Flux<FilePart> imageFiles) {
        // 1. Subir imágenes a Cloudinary
        return cloudinaryService.uploadMultipleImages(imageFiles, "motels")
                .collectList()
                .flatMap(imageUrls -> {
                    // 2. Crear motel con las URLs de Cloudinary
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
                            motel.longitude()
                    );
                    
                    return motelRepositoryPort.save(motelWithImages);
                });
    }

    /**
     * Actualiza un motel y reemplaza sus imágenes
     * 
     * @param id ID del motel
     * @param motel Datos actualizados
     * @param imageFiles Nuevas imágenes (opcional)
     * @return Motel actualizado
     */
    public Mono<Motel> updateMotelWithImages(
            Long id,
            Motel motel,
            Flux<FilePart> imageFiles) {
        
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Motel no encontrado")))
                .flatMap(existingMotel -> {
                    // Si hay nuevas imágenes
                    if (imageFiles != null) {
                        // 1. Eliminar imágenes antiguas de Cloudinary
                        return deleteExistingImages(existingMotel.imageUrls())
                                .then(cloudinaryService.uploadMultipleImages(imageFiles, "motels")
                                        .collectList())
                                .flatMap(newUrls -> {
                                    // 2. Actualizar motel con nuevas URLs
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
                                            motel.longitude()
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
                                existingMotel.imageUrls(), // Mantener imágenes existentes
                                motel.latitude(),
                                motel.longitude()
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
                                // Combinar URLs existentes con nuevas
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
                                        existingMotel.longitude()
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
                    // Eliminar de Cloudinary
                    return Flux.fromIterable(imageUrlsToRemove)
                            .flatMap(url -> {
                                String publicId = cloudinaryService.extractPublicId(url);
                                return publicId != null 
                                        ? cloudinaryService.deleteImage(publicId)
                                        : Mono.empty();
                            })
                            .then(Mono.defer(() -> {
                                // Actualizar motel sin las URLs eliminadas
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
                                        existingMotel.longitude()
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