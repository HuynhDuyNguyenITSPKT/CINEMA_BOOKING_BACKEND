package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.response.BookingResponse;

import java.util.List;

public interface IBookingService {
    /** Tạo booking draft qua đúng Template Method flow. */
    BookingResponse createBooking(BookingRequest request, String username);
    BookingResponse getBookingById(String bookingId, String username);
    List<BookingResponse> getMyBookings(String username);
    BookingResponse cancelBooking(String bookingId, String username);
}
