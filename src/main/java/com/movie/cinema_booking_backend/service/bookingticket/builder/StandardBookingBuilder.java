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
public class StandardBookingBuilder extends AbstractBookingBuilder {

    public StandardBookingBuilder() {
        super();
    }

    @Override
    public void validateRules() {
        // Luồng bán lẻ Max 8 vé
        if (seats.size() > 8) {
            throw new AppException(ErrorCode.BOOKING_MAX_SEATS_EXCEEDED);
        }

        // Tự động đóng đặt vé online 30 phút trước giờ chiếu
        if (LocalDateTime.now().plusMinutes(30).isAfter(showtime.getStartTime())) {
            throw new AppException(ErrorCode.BOOKING_CLOSED_BEFORE_SHOWTIME);
        }

        // Optional: Nếu có field Age Rating ở Movie thì check với User DateOfBirth ở đây
        // VD: throw new AppException(ErrorCode.AGE_RESTRICTION_NOT_MET);
    }

    @Override
    public void buildEntities() {
        this.booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.RESERVED) // Trạng thái: Giữ chỗ chờ thanh toán
                .grandTotalPrice(calcResult.getFinalTotal())
                .createdAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        for (Seat seat : seats) {
            BigDecimal price = calcResult.getTicketPrices().getOrDefault(seat.getId(), BigDecimal.ZERO);
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime)
                    .seat(seat)
                    .finalPrice(price)
                    .status(TicketStatus.PROCESSING)
                    .build();

            // Nếu có khuyến mãi, gán vào vé
            if (promotion != null && calcResult.getPromotionDiscount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountPerTicket = calcResult.getPromotionDiscount()
                        .divide(BigDecimal.valueOf(seats.size()), 0, java.math.RoundingMode.HALF_UP);

                TicketPromotion tp = TicketPromotion.builder()
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
                BookingExtra be = BookingExtra.builder()
                        .extraService(ex).quantity(qty)
                        .totalPrice(ex.getUnitPrice().multiply(BigDecimal.valueOf(qty)))
                        .build();
                booking.addBookingExtra(be);
            }
        }
    }
}
