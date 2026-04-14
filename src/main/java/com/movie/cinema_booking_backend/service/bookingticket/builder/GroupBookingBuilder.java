package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupBookingBuilder extends AbstractBookingBuilder {

    public GroupBookingBuilder() {
        super();
    }

    @Override
    public void validateRules() {
        // Luồng khách đoàn yêu cầu tối thiểu 20 vé
        if (seats.size() < 20) {
            throw new AppException(ErrorCode.BOOKING_MIN_SEATS_REQUIRED);
        }

        // Block không cho Khách đoàn nhập mã giảm giá (vì đã có chiết khấu B2B rồi)
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Khách đoàn không được áp dụng thêm mã Voucher!");
        }
    }


    @Override
    public void buildEntities() {
        this.booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.PENDING_APPROVAL) // B2B: Lưu lại nhưng Chờ Admin Duyệt
                .grandTotalPrice(calcResult.getFinalTotal()) // Chú ý: Ở hệ thống thực tế Group có thể ghi đè grandTotalPrice
                .createdAt(LocalDateTime.now())
                .note("[GROUP - B2B] " + (request.getNote() != null ? request.getNote() : ""))
                .build();

        for (Seat seat : seats) {
            BigDecimal price = calcResult.getTicketPrices().getOrDefault(seat.getId(), BigDecimal.ZERO);
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime).seat(seat).finalPrice(price).status(TicketStatus.PROCESSING)
                    .build();
            booking.addTicket(ticket);
        }

        if (extraServices != null) {
            for (ExtraService ex : extraServices) {
                int qty = request.getExtras().get(ex.getId());
                booking.addBookingExtra(BookingExtra.builder()
                        .extraService(ex).quantity(qty)
                        .totalPrice(ex.getUnitPrice().multiply(BigDecimal.valueOf(qty))).build());
            }
        }
    }
}
