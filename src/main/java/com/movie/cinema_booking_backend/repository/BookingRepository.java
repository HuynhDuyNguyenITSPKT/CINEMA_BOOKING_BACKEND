package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByUserId(Long userId);

    /** "My bookings" via JWT username — không cần load User entity trước */
    List<Booking> findByUser_Account_Username(String username);
    List<Booking> findByStatus(BookingStatus status);
        List<Booking> findByStatusIn(List<BookingStatus> statuses);
        List<Booking> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
        List<Booking> findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus status,
            LocalDateTime start,
            LocalDateTime end
        );
        List<Booking> findByStatusInAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            List<BookingStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
        );
        long countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus status,
            LocalDateTime start,
            LocalDateTime end
        );
        long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
        List<Booking> findTop50ByStatusAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            BookingStatus status,
            LocalDateTime fromDateTime
        );


    /**
     * Load Booking kèm tickets.
     * Không fetch đồng thời nhiều collection kiểu List để tránh MultipleBagFetchException.
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.tickets t " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") String id);
}
