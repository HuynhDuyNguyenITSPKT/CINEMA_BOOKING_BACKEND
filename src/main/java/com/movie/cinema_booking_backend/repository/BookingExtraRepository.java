package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.BookingExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BookingExtraRepository extends JpaRepository<BookingExtra, Long> {
	List<BookingExtra> findByBookingIdIn(Collection<String> bookingIds);
}
