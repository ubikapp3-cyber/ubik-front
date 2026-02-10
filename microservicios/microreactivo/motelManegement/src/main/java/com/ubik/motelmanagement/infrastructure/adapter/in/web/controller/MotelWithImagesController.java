package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.service.MotelServiceWithImages;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;  
import org.springframework.web.server.ServerWebExchange;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador extendido para Moteles con gestión de imágenes
 * 
 * NUEVOS ENDPOINTS:
 * - POST /api/motels/with-images - Crea un motel con imágenes
 * - POST /api/motels/{id}/images - Agrega imágenes a un motel
 * - DELETE /api/motels/{id}/images - Elimina imágenes específicas
 * - PUT /api/motels/{id}/with-images - Actualiza motel con nuevas imágenes
 */
@RestController
@RequestMapping("/api/motels")
public class MotelWithImagesController {

    private final MotelServiceWithImages motelServiceWithImages;
    private final MotelDtoMapper motelDtoMapper;

    public MotelWithImagesController(
            MotelServiceWithImages motelServiceWithImages,
            MotelDtoMapper motelDtoMapper) {
        this.motelServiceWithImages = motelServiceWithImages;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * Crea un motel con imágenes
     * POST /api/motels/with-images
     * 
     * FormData:
     * - motelData: JSON con datos del motel
     * - images: Array de archivos de imagen
     */
@PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@ResponseStatus(HttpStatus.CREATED)
public Mono<MotelResponse> createMotelWithImages(
        @RequestPart("motelData") CreateMotelRequest motelData,
        @RequestPart("images") Flux<FilePart> images,
        ServerWebExchange exchange) {  // ✅ Agregar ServerWebExchange
    
    String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
    if (userIdHeader == null) {
        return Mono.error(new RuntimeException("Usuario no autenticado"));
    }
    
    Long userId = Long.parseLong(userIdHeader);
    Motel motel = motelDtoMapper.toDomain(motelData);
    
    // ✅ Reemplazar propertyId con el userId del token
    Motel motelWithOwner = new Motel(
            motel.id(), motel.name(), motel.address(), motel.phoneNumber(),
            motel.description(), motel.city(),
            userId,  // ✅ propertyId = userId autenticado
            motel.dateCreated(), motel.imageUrls(), motel.latitude(), motel.longitude(),
            motel.approvalStatus(), motel.approvalDate(), motel.approvedByUserId(),
            motel.rejectionReason(), motel.rues(), motel.rnt(), motel.ownerDocumentType(),
            motel.ownerDocumentNumber(), motel.ownerFullName(),
            motel.legalRepresentativeName(), motel.legalDocumentUrl()
    );
    
    return motelServiceWithImages.createMotelWithImages(motelWithOwner, images)
            .map(motelDtoMapper::toResponse);
}

    /**
     * Agrega imágenes adicionales a un motel existente
     * POST /api/motels/{id}/images
     * 
     * FormData:
     * - images: Array de archivos de imagen
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<MotelResponse> addImagesToMotel(
            @PathVariable Long id,
            @RequestPart("images") Flux<FilePart> images) {
        
        return motelServiceWithImages.addImagesToMotel(id, images)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Actualiza un motel y reemplaza todas sus imágenes
     * PUT /api/motels/{id}/with-images
     * 
     * FormData:
     * - motelData: JSON con datos actualizados
     * - images: Array de nuevas imágenes (opcional)
     */
    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<MotelResponse> updateMotelWithImages(
            @PathVariable Long id,
            @RequestPart("motelData") CreateMotelRequest motelData,
            @RequestPart(value = "images", required = false) Flux<FilePart> images) {
        
        Motel motel = motelDtoMapper.toDomain(motelData);
        
        return motelServiceWithImages.updateMotelWithImages(id, motel, images)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Elimina imágenes específicas de un motel
     * DELETE /api/motels/{id}/images
     * 
     * Body: ["url1", "url2", ...]
     */
    @DeleteMapping("/{id}/images")
    public Mono<MotelResponse> removeImagesFromMotel(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls) {
        
        return motelServiceWithImages.removeImagesFromMotel(id, imageUrls)
                .map(motelDtoMapper::toResponse);
    }
}