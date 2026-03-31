package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingInitiateResponse {
    private String bookingId;
    private String paymentUrl;
    private Instant expiresAt; // Thời gian khoá ghế hết hạn
}
