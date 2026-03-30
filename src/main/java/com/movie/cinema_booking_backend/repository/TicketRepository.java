package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    /**
     * Lấy tập hợp seatId đã có Ticket với status BOOKED trong một showtime.
     * Dùng bởi SeatValidationProxy để check "ghế đã được đặt" (DB check).
     *
     * Trả về Set<String> thay vì List<Ticket> để:
     *  1. Lookup O(1) thay vì O(n)
     *  2. Không load toàn bộ Ticket entity, chỉ lấy seat_id (projection)
     */
    @Query("SELECT t.seat.id FROM Ticket t " +
           "WHERE t.showtime.id = :showtimeId " +
           "AND t.status = :status")
    Set<String> findSeatIdsByShowtimeIdAndStatus(
            @Param("showtimeId") String showtimeId,
            @Param("status") TicketStatus status);
}
