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
     * Dùng cho GET /api/auditoriums/{id}/seats (Phase 1) và regenerateSeats (Phase 0).
     */
    List<Seat> findByAuditoriumId(String auditoriumId);

    /**
     * Lấy ghế theo auditoriumId, kèm fetch SeatType để tránh N+1.
     * Dùng trong SeatServiceImpl và SeatValidationProxy (Phase 2).
     */
    @Query("SELECT s FROM Seat s JOIN FETCH s.seatType WHERE s.auditorium.id = :auditoriumId")
    List<Seat> findByAuditoriumIdWithSeatType(@Param("auditoriumId") String auditoriumId);

    /**
     * Lấy ghế theo showtimeId (qua Auditorium của Showtime), kèm fetch SeatType.
     * Dùng cho GET /api/showtimes/{id}/seat-map (Phase 2 Proxy).
     *
     * Logic: Showtime → Auditorium → Seats.
     * Không cần bảng join riêng vì Showtime đã có auditorium_id FK.
     */
    @Query("SELECT s FROM Seat s JOIN FETCH s.seatType " +
           "WHERE s.auditorium.id = " +
           "(SELECT st.auditorium.id FROM Showtime st WHERE st.id = :showtimeId)")
    List<Seat> findByShowtimeIdWithSeatType(@Param("showtimeId") String showtimeId);
}

