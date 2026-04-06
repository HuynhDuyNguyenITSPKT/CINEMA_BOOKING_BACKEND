package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByUserId(Long userId);

    /** "My bookings" via JWT username — không cần load User entity trước */
    List<Booking> findByUser_Account_Username(String username);
    List<Booking> findByStatus(BookingStatus status);


    /**
     * Load Booking kèm Tickets + BookingExtras trong 1 query.
     * Tránh N+1 khi map sang BookingResponse chi tiết.
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.tickets t " +
           "LEFT JOIN FETCH b.bookingExtras " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") String id);
}
