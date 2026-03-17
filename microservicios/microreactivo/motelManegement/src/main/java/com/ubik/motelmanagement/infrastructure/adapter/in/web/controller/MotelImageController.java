package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.infrastructure.service.MotelImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/motels")
public class MotelImageController {

    private final MotelImageService motelImageService;

    public MotelImageController(MotelImageService motelImageService) {
        this.motelImageService = motelImageService;
    }

    public record UrlRequest(String url) {}

    @PutMapping("/{id}/images/profile")
    public Mono<ResponseEntity<Void>> setProfile(@PathVariable Long id, @RequestBody Mono<UrlRequest> body) {
        return body.flatMap(b -> motelImageService.setProfileImage(id, b.url()))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}/images/cover")
    public Mono<ResponseEntity<Void>> setCover(@PathVariable Long id, @RequestBody Mono<UrlRequest> body) {
        return body.flatMap(b -> motelImageService.setCoverImage(id, b.url()))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/images/gallery")
    public Mono<ResponseEntity<Void>> addGallery(@PathVariable Long id, @RequestBody Mono<UrlRequest> body) {
        return body.flatMap(b -> motelImageService.addGalleryImage(id, b.url()))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id, @PathVariable Long imageId) {
        return motelImageService.deleteImage(id, imageId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
