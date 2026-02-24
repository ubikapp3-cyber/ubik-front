package com.ubik.usermanagement.infrastructure.adapter.out.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de persistencia para usuarios en PostgreSQL
 */
@Table("users")
public record UserEntity(

        @Id
        Long id,

        @Column("username")
        String username,

        @Column("password")
        String password,

        @Column("email")
        String email,

        @Column("phone_number")
        String phoneNumber,

        @Column("registration_time")
        LocalDateTime createdAt,

        @Column("anonymous")
        boolean anonymous,

        @Column("role_id")
        Long roleId,

        @Column("reset_token")
        String resetToken,

        @Column("reset_token_expiry")
        LocalDateTime resetTokenExpiry,

        @Column("longitude")
        BigDecimal longitude,

        @Column("latitude")
        BigDecimal latitude,

        @Column("birth_date")
        LocalDate birthDate
) {
}