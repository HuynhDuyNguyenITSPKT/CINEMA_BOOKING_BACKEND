package com.movie.cinema_booking_backend.service.bookingticket.observer;

import java.math.BigDecimal;
import java.util.List;

public record BookingSuccessEvent(
        String bookingId,
        String userEmail,
        String movieName,
        String showtimeId,
        List<String> seatIds,
        List<String> ticketIds,
        BigDecimal grandTotal,
        String promotionCode
) {
}
