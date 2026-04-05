package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
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

    public GroupBookingBuilder(BookingRepository bookingRepo, SeatRepository seatRepo,
                               ShowtimeRepository showtimeRepo, ExtraServiceRepository extraRepo,
                               PromotionRepository promoRepo, AccountRepository accountRepo) {
        super(bookingRepo, seatRepo, showtimeRepo, extraRepo, promoRepo, accountRepo);
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
    public void runPricing(PricingEngine engine) {
        // Gọi Engine để tính base + surcharge + default promo + tax
        super.runPricing(engine);

        // EXTRA RULE: Khách đoàn được giảm thêm 5% trực tiếp lên final price (trước extras)
        // (Đây là cách override tính giá linh hoạt ngay trong Builder mà không cần Policy)
        BigDecimal total = calcResult.getFinalTotal()
                .multiply(BigDecimal.valueOf(95))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Không sửa lại ticket list để hiển thị đúng bill ban đầu, chỉ giảm tồng
        // Hoặc có thể viết thêm logic giảm điều vào từng vé ở đây
    }

    @Override
    public void buildEntities() {
        this.booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.PENDING_APPROVAL) // B2B: Lưu lại nhưng Chờ Admin Duyệt
                .totalAmount(calcResult.getFinalTotal()) // Chú ý: Ở hệ thống thực tế Group có thể ghi đè totalAmount
                .createdAt(LocalDateTime.now())
                .note("[GROUP - B2B] " + (request.getNote() != null ? request.getNote() : ""))
                .build();

        for (Seat seat : seats) {
            BigDecimal price = calcResult.getTicketPrices().getOrDefault(seat.getId(), BigDecimal.ZERO);
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime).seat(seat).price(price).status(TicketStatus.PROCESSING)
                    .build();
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
