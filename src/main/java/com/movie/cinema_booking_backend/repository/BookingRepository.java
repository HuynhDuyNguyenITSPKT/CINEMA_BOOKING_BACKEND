package com.movie.cinema_booking_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movie.cinema_booking_backend.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
}
