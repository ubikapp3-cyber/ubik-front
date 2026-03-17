package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("motel_images")
public record MotelImageEntity(
        @Id Integer id,
        @Column("motel_id") Integer motelId,
        @Column("image_url") String imageUrl,
        @Column("order_index") Integer orderIndex,
        String role,
        @Column("created_at") LocalDateTime createdAt
) {}
