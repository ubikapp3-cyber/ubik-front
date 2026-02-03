package com.ubik.motelmanagement.infrastructure.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestionar la subida de imágenes a Cloudinary
 * 
 * Este servicio maneja:
 * - Subida de imágenes individuales
 * - Subida de múltiples imágenes
 * - Eliminación de imágenes
 * - Optimización automática de imágenes
 */
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Sube una imagen a Cloudinary
     * 
     * @param filePart Archivo de imagen
     * @param folder Carpeta en Cloudinary donde se guardará
     * @return Mono con la URL pública de la imagen
     */
    public Mono<String> uploadImage(FilePart filePart, String folder) {
        return convertFilePartToBytes(filePart)
                .flatMap(bytes -> uploadToCloudinary(bytes, folder));
    }

    /**
     * Sube múltiples imágenes a Cloudinary
     * 
     * @param fileParts Flujo de archivos de imagen
     * @param folder Carpeta en Cloudinary
     * @return Flux con las URLs públicas de las imágenes
     */
    public Flux<String> uploadMultipleImages(Flux<FilePart> fileParts, String folder) {
        return fileParts.flatMap(filePart -> uploadImage(filePart, folder));
    }

    /**
     * Elimina una imagen de Cloudinary
     * 
     * @param publicId ID público de la imagen en Cloudinary
     * @return Mono que completa cuando se elimina la imagen
     */
    public Mono<Void> deleteImage(String publicId) {
        return Mono.fromCallable(() -> {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return null;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     * 
     * @param imageUrl URL de la imagen
     * @return Public ID extraído
     */
    public String extractPublicId(String imageUrl) {

        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            
            String afterUpload = parts[1];
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            return withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convierte un FilePart reactivo a byte array
     */
    private Mono<byte[]> convertFilePartToBytes(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }

    /**
     * Sube bytes a Cloudinary
     */
    private Mono<String> uploadToCloudinary(byte[] bytes, String folder) {
        return Mono.fromCallable(() -> {
            String publicId = UUID.randomUUID().toString();
            
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "image",
                    "overwrite", true,
                    "transformation", ObjectUtils.asMap(
                            "quality", "auto:good",
                            "fetch_format", "auto"
                    )
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    new ByteArrayInputStream(bytes),
                    uploadParams
            );

            return (String) uploadResult.get("secure_url");
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}