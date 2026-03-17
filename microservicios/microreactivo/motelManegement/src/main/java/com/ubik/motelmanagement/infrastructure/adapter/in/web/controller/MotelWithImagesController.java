package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;


import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.service.MotelServiceWithImages;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador extendido para Moteles con gestión de imágenes.
 *
 * Endpoints:
 * - POST   /api/motels/with-images         (json) crea motel con URLs de imágenes (GALLERY)
 * - POST   /api/motels/{id}/images         (json) agrega URLs de imágenes (GALLERY)
 * - PUT    /api/motels/{id}/with-images    (json) actualiza motel y reemplaza galería con URLs
 * - DELETE /api/motels/{id}/images         (json array urls) elimina imágenes por URL
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

    @PostMapping(value = "/with-images", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotelWithImages(
            @RequestBody CreateMotelRequest motelData,
            ServerWebExchange exchange) {

        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }

        Long userId = Long.parseLong(userIdHeader);
        Motel motel = motelDtoMapper.toDomain(motelData);

        // propertyId = userId autenticado (mismo patrón que ya usas)
        Motel motelWithOwner = new Motel(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                userId,
                motel.dateCreated(),
                motel.imageUrls(),
                motel.latitude(),
                motel.longitude(),
                motel.approvalStatus(),
                motel.approvalDate(),
                motel.approvedByUserId(),
                motel.rejectionReason(),
                motel.rues(),
                motel.rnt(),
                motel.ownerDocumentType(),
                motel.ownerDocumentNumber(),
                motel.ownerFullName(),
                motel.legalRepresentativeName(),
                motel.legalDocumentUrl()
        );

        return motelServiceWithImages.createMotelWithImages(motelWithOwner)
                .map(motelDtoMapper::toResponse);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MotelResponse> addImagesToMotel(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls) {

        return motelServiceWithImages.addImagesToMotel(id, imageUrls)
                .map(motelDtoMapper::toResponse);
    }

    @PutMapping(value = "/{id}/with-images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MotelResponse> updateMotelWithImages(
            @PathVariable Long id,
            @RequestBody UpdateMotelRequest motelData) {

        Motel motel = motelDtoMapper.toDomain(motelData);

        return motelServiceWithImages.updateMotelWithImages(id, motel)
                .map(motelDtoMapper::toResponse);
    }



    @DeleteMapping("/{id}/images")
    public Mono<MotelResponse> removeImagesFromMotel(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls) {

        return motelServiceWithImages.removeImagesFromMotel(id, imageUrls)
                .map(motelDtoMapper::toResponse);
    }
}