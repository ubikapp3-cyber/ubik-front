package com.example.paymentservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("motel_mp_accounts")
public record MotelMpAccountEntity(
        @Id Long id,
        @Column("motel_id") Long motelId,
        @Column("mp_user_id") String mpUserId,
        @Column("access_token") String accessToken,
        @Column("refresh_token") String refreshToken,
        @Column("token_expires_at") LocalDateTime tokenExpiresAt,
        @Column("mp_email") String mpEmail,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt
) {}