package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;

public record CreateMotelRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 3, max = 100) String name,
        @NotBlank(message = "La dirección es requerida")
        @Size(max = 255) String address,
        @Size(max = 20) String phoneNumber,
        @Size(max = 500) String description,
        @NotBlank(message = "La ciudad es requerida")
        @Size(max = 100) String city,

        Long propertyId,

        @Size(max = 10, message = "No se pueden agregar más de 10 imágenes")
        List<@Size(max = 500) String> imageUrls,

        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,

        @NotBlank(message = "El RUES es requerido") String rues,
        @NotBlank(message = "El RNT es requerido") String rnt,
        @NotBlank(message = "El tipo de documento es requerido") String ownerDocumentType,
        @NotBlank(message = "El número de documento es requerido") String ownerDocumentNumber,
        @NotBlank(message = "El nombre del propietario es requerido") String ownerFullName,
        String legalRepresentativeName,
        String legalDocumentUrl
) {
        // Constructor compacto para asegurar que la lista de imágenes nunca sea null
        public CreateMotelRequest {
                if (imageUrls == null) {
                        imageUrls = new ArrayList<>();
                }
        }
}