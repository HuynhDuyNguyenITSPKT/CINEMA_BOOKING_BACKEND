package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.response.SeatResponse;

import java.util.List;

/**
 * Interface cho Seat service.
 *
 * Phase 1: getSeatsByAuditorium() — query thô từ DB, status = null.
 * Phase 2: interface này được dùng làm Subject trong Proxy Pattern.
 *          SeatValidationProxy sẽ implement @Primary, gọi real rồi
 *          enrich thêm status = AVAILABLE/LOCKED/BOOKED.
 */
public interface ISeatService {
    List<SeatResponse> getSeatsByAuditorium(String auditoriumId);
}

