package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.service.MotelServiceWithImages;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.security.AuthContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador para operaciones de Motel que involucran subida de imágenes.
 *
 * CAMBIOS respecto a la versión anterior:
 * - Usa AuthContextResolver para resolver userId por username (igual que MotelController)
 * - Valida el rol antes de procesar la request
 * - Elimina el uso directo de X-User-Id del header
 * - Usa motelDtoMapper.toDomainWithOwner() para evitar duplicar la construcción del record
 */
@RestController
@RequestMapping("/api/motels")
public class MotelWithImagesController {

    private static final Logger log = LoggerFactory.getLogger(MotelWithImagesController.class);

    private final String roleIdAdmin;
    private final String roleIdPropertyOwner;

    private final MotelServiceWithImages motelServiceWithImages;
    private final MotelDtoMapper motelDtoMapper;
    private final AuthContextResolver authContextResolver;

    public MotelWithImagesController(
            MotelServiceWithImages motelServiceWithImages,
            MotelDtoMapper motelDtoMapper,
            AuthContextResolver authContextResolver,
            @Value("${app.roles.admin:#{environment['ROLE_ID_ADMIN']}}") String roleIdAdmin,
            @Value("${app.roles.property-owner:#{environment['ROLE_ID_PROPERTY_OWNER']}}") String roleIdPropertyOwner) {
        this.motelServiceWithImages = motelServiceWithImages;
        this.motelDtoMapper = motelDtoMapper;
        this.authContextResolver = authContextResolver;
        this.roleIdAdmin = roleIdAdmin;
        this.roleIdPropertyOwner = roleIdPropertyOwner;
    }

    /**
     * POST /api/motels/with-images
     * Crea un motel con imágenes.
     *
     * FormData:
     * - motelData: JSON con datos del motel
     * - images:    archivos de imagen
     */
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotelWithImages(
            @RequestPart("motelData") CreateMotelRequest motelData,
            @RequestPart("images") Flux<FilePart> images,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para crear moteles"));
        }

        log.info("Creando motel con imágenes — username: '{}', role: '{}'",
                authContextResolver.extractUsername(exchange), role);

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId -> {
                    log.info("UserId resuelto: {} para motel '{}'", userId, motelData.name());
                    return motelServiceWithImages.createMotelWithImages(
                            motelDtoMapper.toDomainWithOwner(motelData, userId),
                            images
                    );
                })
                .map(motelDtoMapper::toResponse);
    }

    /**
     * POST /api/motels/{id}/images
     * Agrega imágenes adicionales a un motel existente.
     * Requiere ser el propietario del motel o admin.
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<MotelResponse> addImagesToMotel(
            @PathVariable Long id,
            @RequestPart("images") Flux<FilePart> images,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para agregar imágenes a este motel"));
        }

        log.info("Agregando imágenes a motel {} — username: '{}'",
                id, authContextResolver.extractUsername(exchange));

        return motelServiceWithImages.addImagesToMotel(id, images)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PUT /api/motels/{id}/with-images
     * Actualiza un motel reemplazando todas sus imágenes.
     * Admin actualiza cualquier motel. Property owner solo los suyos.
     */
    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<MotelResponse> updateMotelWithImages(
            @PathVariable Long id,
            @RequestPart("motelData") CreateMotelRequest motelData,
            @RequestPart(value = "images", required = false) Flux<FilePart> images,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para modificar moteles"));
        }

        log.info("Actualizando motel {} con imágenes — username: '{}', role: '{}'",
                id, authContextResolver.extractUsername(exchange), role);

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId ->
                        motelServiceWithImages.updateMotelWithImages(
                                id,
                                motelDtoMapper.toDomainWithOwner(motelData, userId),
                                images
                        )
                )
                .map(motelDtoMapper::toResponse);
    }

    /**
     * DELETE /api/motels/{id}/images
     * Elimina imágenes específicas de un motel.
     * Body: ["url1", "url2", ...]
     */
    @DeleteMapping("/{id}/images")
    public Mono<MotelResponse> removeImagesFromMotel(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para eliminar imágenes de este motel"));
        }

        log.info("Eliminando {} imágenes del motel {} — username: '{}'",
                imageUrls.size(), id, authContextResolver.extractUsername(exchange));

        return motelServiceWithImages.removeImagesFromMotel(id, imageUrls)
                .map(motelDtoMapper::toResponse);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private boolean isAdmin(String role) {
        return roleIdAdmin != null && roleIdAdmin.equals(role);
    }

    private boolean isPropertyOwner(String role) {
        return roleIdPropertyOwner != null && roleIdPropertyOwner.equals(role);
    }
}