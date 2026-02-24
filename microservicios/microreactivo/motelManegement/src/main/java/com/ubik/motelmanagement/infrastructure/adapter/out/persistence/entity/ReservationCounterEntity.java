package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar contadores de códigos de confirmación por día
 */
@Table("reservation_counters")
public record ReservationCounterEntity(
        @Id LocalDate date,
        Integer counter,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt
) {
    /**
     * Constructor para crear nuevo contador del día
     */
    public static ReservationCounterEntity createForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        return new ReservationCounterEntity(today, 0, now, now);
    }

    /**
     * Incrementa el contador
     */
    public ReservationCounterEntity increment() {
        return new ReservationCounterEntity(
                this.date,
                this.counter + 1,
                this.createdAt,
                LocalDateTime.now()
        );
    }
}