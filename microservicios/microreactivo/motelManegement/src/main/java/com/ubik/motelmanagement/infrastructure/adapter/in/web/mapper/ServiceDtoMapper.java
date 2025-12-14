package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Service;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DTOs web y modelo de dominio Service
 */
@Component
public class ServiceDtoMapper {

    /**
     * Convierte CreateServiceRequest a Service de dominio
     */
    public Service toDomain(CreateServiceRequest request) {
        if (request == null) {
            return null;
        }
        return new Service(
                null, // El ID se generará en la BD
                request.name(),
                request.description(),
                request.icon(),
                null // El timestamp se generará en la BD
        );
    }

    /**
     * Convierte UpdateServiceRequest a Service de dominio (sin ID ni createdAt)
     */
    public Service toDomain(UpdateServiceRequest request) {
        if (request == null) {
            return null;
        }
        return new Service(
                null, // Se establecerá en el servicio
                request.name(),
                request.description(),
                request.icon(),
                null // Se mantendrá el existente
        );
    }

    /**
     * Convierte Service de dominio a ServiceResponse
     */
    public ServiceResponse toResponse(Service service) {
        if (service == null) {
            return null;
        }
        return new ServiceResponse(
                service.id(),
                service.name(),
                service.description(),
                service.icon(),
                service.createdAt()
        );
    }
}
