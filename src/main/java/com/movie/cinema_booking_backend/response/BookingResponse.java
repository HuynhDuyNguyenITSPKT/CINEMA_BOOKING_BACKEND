package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookingResponse {
    private String id;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String note;

    // Showtime info (mock-safe: có thể null nếu showtime chưa có data)
    private String showtimeId;
    private String movieName;
    private String auditoriumName;
    private LocalDateTime startTime;

    private List<TicketResponse> tickets;
    private List<BookingExtraResponse> extras;
}
