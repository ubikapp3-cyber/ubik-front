package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.domain.model.WeeklyRevenue;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repositorio R2DBC para gestionar reservas
 * ✅ Incluye queries para confirmationCode
 */
@Repository
public interface ReservationR2dbcRepository extends R2dbcRepository<ReservationEntity, Long> {

    Flux<ReservationEntity> findByRoomId(Long roomId);

    Flux<ReservationEntity> findByUserId(Long userId);

    @Query("SELECT * FROM reservations WHERE room_id = :roomId " +
            "AND status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND ((check_in_date BETWEEN :startDate AND :endDate) " +
            "OR (check_out_date BETWEEN :startDate AND :endDate) " +
            "OR (check_in_date <= :startDate AND check_out_date >= :endDate))")
    Flux<ReservationEntity> findConflictingReservations(Long roomId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT * FROM reservations WHERE room_id = :roomId " +
            "AND id != :excludeId " +
            "AND status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
            "AND ((check_in_date BETWEEN :startDate AND :endDate) " +
            "OR (check_out_date BETWEEN :startDate AND :endDate) " +
            "OR (check_in_date <= :startDate AND check_out_date >= :endDate))")
    Flux<ReservationEntity> findConflictingReservationsExcluding(Long roomId, LocalDateTime startDate, LocalDateTime endDate, Long excludeId);

    @Query("SELECT EXISTS(SELECT 1 FROM reservations " +
            "WHERE confirmation_code = :code " +
            "AND status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN'))")
    Mono<Boolean> existsActiveReservationWithCode(String code);

    @Query("SELECT * FROM reservations WHERE confirmation_code = :code")
    Mono<ReservationEntity> findByConfirmationCode(String code);

    @Query("SELECT r.* FROM reservations r " +
            "JOIN room rm ON rm.id = r.room_id " +
            "WHERE rm.motel_id = :motelId " +
            "AND (CAST(r.check_in_date AS DATE) = CURRENT_DATE OR CAST(r.check_out_date AS DATE) = CURRENT_DATE) " +
            "AND r.status NOT IN ('CANCELLED')")
    Flux<ReservationEntity> findTodayByMotelId(Long motelId);

    @Query("SELECT CAST(r.check_in_date AS DATE) AS day, SUM(r.total_price) AS revenue " +
            "FROM reservations r " +
            "JOIN room rm ON rm.id = r.room_id " +
            "WHERE rm.motel_id = :motelId " +
            "AND r.check_in_date >= CURRENT_DATE - INTERVAL '6 days' " +
            "AND r.status != 'CANCELLED' " +
            "GROUP BY CAST(r.check_in_date AS DATE) " +
            "ORDER BY day")
    Flux<WeeklyRevenue> findWeeklyRevenueByMotelId(Long motelId);
}