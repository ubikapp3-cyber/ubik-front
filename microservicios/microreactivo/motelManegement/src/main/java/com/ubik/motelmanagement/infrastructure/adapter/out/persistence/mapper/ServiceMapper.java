package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.Service;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.ServiceEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre el modelo de dominio Service y la entidad de persistencia ServiceEntity
 */
@Component
public class ServiceMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio
     */
    public Service toDomain(ServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Service(
                entity.id(),
                entity.name(),
                entity.description(),
                entity.icon(),
                entity.createdAt()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public ServiceEntity toEntity(Service service) {
        if (service == null) {
            return null;
        }
        return new ServiceEntity(
                service.id(),
                service.name(),
                service.description(),
                service.icon(),
                service.createdAt()
        );
    }
}
