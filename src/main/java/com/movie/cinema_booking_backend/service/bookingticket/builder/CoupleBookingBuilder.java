package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CoupleBookingBuilder extends AbstractBookingBuilder {

    public CoupleBookingBuilder() {
        super();
    }

    @Override
    public void validateRules() {
        // Bắt buộc phải chọn chẵn ghế (bội số của 2)
        if (seats.size() % 2 != 0) {
            throw new AppException(ErrorCode.COUPLE_SEATS_MUST_BE_EVEN);
        }

        // Bắt buộc tất cả các ghế phải là loại SWEETBOX
        boolean hasNonSweetbox = seats.stream().anyMatch(
                seat -> seat.getSeatType() == null || !"SWEETBOX".equalsIgnoreCase(seat.getSeatType().getName())
        );

        if (hasNonSweetbox) {
            throw new AppException(ErrorCode.COUPLE_BOOKING_ONLY_ALLOWS_SWEETBOX);
        }
    }

    @Override
    public void buildEntities() {
        this.booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.RESERVED) // Trạng thái: Giữ chỗ chờ thanh toán
                .totalAmount(calcResult.getFinalTotal())
                .createdAt(LocalDateTime.now())
                .note("[COUPLE] " + (request.getNote() != null ? request.getNote() : ""))
                .build();

        for (Seat seat : seats) {
            BigDecimal price = calcResult.getTicketPrices().getOrDefault(seat.getId(), BigDecimal.ZERO);
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime).seat(seat).price(price).status(TicketStatus.PROCESSING)
                    .build();

            // Nếu có khuyến mãi, gán vào vé
            if (promotion != null && calcResult.getPromotionDiscount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountPerTicket = calcResult.getPromotionDiscount()
                        .divide(BigDecimal.valueOf(seats.size()), 0, java.math.RoundingMode.HALF_UP);
                        
                TicketPromotion tp = TicketPromotion.builder()
                        .id(new TicketPromotionId(ticket.getId(), promotion.getId()))
                        .ticket(ticket)
                        .promotion(promotion)
                        .discountAmount(discountPerTicket)
                        .appliedDate(LocalDateTime.now())
                        .build();
                ticket.addPromotion(tp);
            }

            booking.addTicket(ticket);
        }

        if (extraServices != null) {
            for (ExtraService ex : extraServices) {
                int qty = request.getExtras().get(ex.getId());
                booking.addBookingExtra(BookingExtra.builder()
                        .extraService(ex).quantity(qty)
                        .totalPrice(ex.getPrice().multiply(BigDecimal.valueOf(qty))).build());
            }
        }
    }
}
