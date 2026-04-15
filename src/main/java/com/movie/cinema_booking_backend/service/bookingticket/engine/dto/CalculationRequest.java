package com.movie.cinema_booking_backend.service.bookingticket.engine.dto;

import com.movie.cinema_booking_backend.entity.ExtraService;
import com.movie.cinema_booking_backend.entity.Promotion;

import java.math.BigDecimal;
import java.util.List;

public record CalculationRequest(

        String bookingType,

        BigDecimal baseTicketPrice,

        List<SeatInfo> seats,

        Promotion promotion,

        List<ExtraLineItem> extras

) {

    public record SeatInfo(String seatId, BigDecimal surchargeAmount) {}

    public record ExtraLineItem(ExtraService extraService, int quantity) {}
}
