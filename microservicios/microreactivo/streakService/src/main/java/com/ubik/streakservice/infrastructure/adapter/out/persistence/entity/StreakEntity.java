package com.ubik.streakservice.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("user_streaks")
public record StreakEntity(
        @Id Long id,
        @Column("user_id")  Long userId,
        String level,
        @Column("reservations_last_30_days") int reservationsLast30Days,
        @Column("discount_rate") double discountRate,
        @Column("calculated_at") LocalDateTime calculatedAt,
        @Column("updated_at")    LocalDateTime updatedAt
) {}
