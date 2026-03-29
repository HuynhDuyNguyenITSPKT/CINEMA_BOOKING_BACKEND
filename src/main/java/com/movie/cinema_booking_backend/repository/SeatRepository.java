package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {

    /**
     * Lấy tất cả ghế thuộc một phòng chiếu.
     * Dùng cho GET /api/auditoriums/{id}/seats (Phase 1).
     */
    List<Seat> findByAuditoriumId(String auditoriumId);

    /**
     * Lấy tất cả ghế thuộc một phòng chiếu, kèm fetch SeatType để tránh N+1.
     * Dùng trong Phase 2 khi Proxy cần enrichWithLockStatus.
     */
    @Query("SELECT s FROM Seat s JOIN FETCH s.seatType WHERE s.auditorium.id = :auditoriumId")
    List<Seat> findByAuditoriumIdWithSeatType(@Param("auditoriumId") String auditoriumId);
}
