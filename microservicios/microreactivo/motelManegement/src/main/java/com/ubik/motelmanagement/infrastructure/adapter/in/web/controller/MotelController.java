package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Motel
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/motels")
public class MotelController {

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public MotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * Crea un nuevo motel
     * POST /api/motels
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotel(@Valid @RequestBody CreateMotelRequest request) {
        return Mono.just(request)
                .map(motelDtoMapper::toDomain)
                .flatMap(motelUseCasePort::createMotel)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene un motel por ID
     * GET /api/motels/{id}
     */
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(@PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene todos los moteles
     * GET /api/motels
     */
    @GetMapping
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene moteles por ciudad
     * GET /api/motels/city/{city}
     */
    @GetMapping("/city/{city}")
    public Flux<MotelResponse> getMotelsByCity(@PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Actualiza un motel existente
     * PUT /api/motels/{id}
     */
    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request) {
        return Mono.just(request)
                .map(motelDtoMapper::toDomain)
                .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Elimina un motel
     * DELETE /api/motels/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(@PathVariable Long id) {
        return motelUseCasePort.deleteMotel(id);
    }
}