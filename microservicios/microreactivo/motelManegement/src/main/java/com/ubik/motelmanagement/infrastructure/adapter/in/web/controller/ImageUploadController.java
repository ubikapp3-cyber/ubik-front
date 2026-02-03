package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.infrastructure.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador para gestionar la subida de imágenes a Cloudinary
 * 
 * ENDPOINTS:
 * - POST /api/images/upload - Sube una imagen
 * - POST /api/images/upload-multiple - Sube múltiples imágenes
 * - DELETE /api/images/{publicId} - Elimina una imagen
 */
@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    public ImageUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Sube una imagen a Cloudinary
     * POST /api/images/upload?folder=motels
     * 
     * @param filePart Archivo de imagen (FormData: file)
     * @param folder Carpeta en Cloudinary (query param, default: "general")
     * @return URL pública de la imagen subida
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ImageUploadResponse> uploadImage(
            @RequestPart("file") FilePart filePart,
            @RequestParam(defaultValue = "general") String folder) {
        
        return cloudinaryService.uploadImage(filePart, folder)
                .map(url -> new ImageUploadResponse(url, "Imagen subida exitosamente"));
    }

    /**
     * Sube múltiples imágenes a Cloudinary
     * POST /api/images/upload-multiple?folder=rooms
     * 
     * @param fileParts Lista de archivos de imagen (FormData: files[])
     * @param folder Carpeta en Cloudinary
     * @return Lista de URLs públicas
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MultipleImageUploadResponse> uploadMultipleImages(
            @RequestPart("files") Flux<FilePart> fileParts,
            @RequestParam(defaultValue = "general") String folder) {
        
        return cloudinaryService.uploadMultipleImages(fileParts, folder)
                .collectList()
                .map(urls -> new MultipleImageUploadResponse(
                        urls,
                        urls.size() + " imágenes subidas exitosamente"
                ));
    }

    /**
     * Elimina una imagen de Cloudinary
     * DELETE /api/images?url=https://...
     * 
     * @param imageUrl URL completa de la imagen
     * @return Mensaje de confirmación
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteImage(@RequestParam String imageUrl) {
        String publicId = cloudinaryService.extractPublicId(imageUrl);
        
        if (publicId == null) {
            return Mono.error(new IllegalArgumentException("URL de imagen inválida"));
        }
        
        return cloudinaryService.deleteImage(publicId);
    }

    /**
     * DTO para respuesta de subida de imagen
     */
    public record ImageUploadResponse(String url, String message) {}

    /**
     * DTO para respuesta de subida múltiple
     */
    public record MultipleImageUploadResponse(List<String> urls, String message) {}
}