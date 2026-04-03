package com.ubik.usermanagement.infrastructure.adapter.out.repository.mapper;

import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.out.repository.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper bidireccional entre User (dominio) y UserEntity (persistencia)
 * 
 * Principio SOLID: Single Responsibility - Solo mapea entre capas
 */
@Component
public class UserMapper {

    /**
     * Convierte UserEntity (persistencia) a User (dominio)
     */
    public User toDomain(UserEntity entity) {
        return new User(
                entity.id(),
                entity.username(),
                entity.password(),
                entity.email(),
                entity.phoneNumber(),
                entity.createdAt(),
                entity.anonymous(),
                entity.roleId(),
                entity.resetToken(),
                entity.resetTokenExpiry(),
                entity.longitude(),
                entity.latitude(),
                entity.birthDate(),
                entity.deletedAt()
        );
    }

    /**
     * Convierte User (dominio) a UserEntity (persistencia)
     */
    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.username(),
                user.password(),
                user.email(),
                user.phoneNumber(),
                user.createdAt(),
                user.anonymous(),
                user.roleId(),
                user.resetToken(),
                user.resetTokenExpiry(),
                user.longitude(),
                user.latitude(),
                user.birthDate(),
                user.deletedAt());
    }
}