package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketResponse {
    private String id;
    private String seatId;
    private String seatName;
    private String seatTypeName;
    private BigDecimal price;
    private TicketStatus status;
    private String qrCodeUrl;
}
