package com.movie.cinema_booking_backend.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookingExtraResponse {
    private Long extraServiceId;
    private String extraServiceName;
    private Integer quantity;
    private BigDecimal totalPrice;
}
