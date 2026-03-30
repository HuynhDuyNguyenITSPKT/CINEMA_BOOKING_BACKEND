package com.movie.cinema_booking_backend.service.bookingticket.template;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.entity.User;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GroupBookingFlow — Luồng đặt vé nhóm (≥ 5 ghế).
 *
 * Override validate() để kiểm tra điều kiện tối thiểu 5 ghế.
 * Override postProcess() để log "gửi group invoice" (mock ở Phase 3,
 *   Phase 5 sẽ implement email thực).
 *
 * @Qualifier("group") → BookingController chọn bean này khi request.bookingType = "GROUP"
 */
@Service
@Qualifier("group")
public class GroupBookingFlow extends BookingFlowTemplate {

    private static final Logger log = LoggerFactory.getLogger(GroupBookingFlow.class);
    private static final int MIN_SEATS_FOR_GROUP = 5;

    public GroupBookingFlow(BookingRepository bookingRepository,
                            SeatRepository seatRepository,
                            ShowtimeRepository showtimeRepository,
                            ExtraServiceRepository extraServiceRepository,
                            PromotionRepository promotionRepository,
                            AccountRepository accountRepository) {
        super(bookingRepository, seatRepository, showtimeRepository,
              extraServiceRepository, promotionRepository, accountRepository);
    }

    /**
     * Rule riêng của Group: phải đặt ít nhất 5 ghế cùng lúc.
     * Gọi trước bước tính tiền để fail-fast mà không vào DB.
     */
    @Override
    protected void validate(BookingRequest request, List<com.movie.cinema_booking_backend.entity.Seat> seats,
                            Showtime showtime, User user) {
        if (seats.size() < MIN_SEATS_FOR_GROUP) {
            throw new AppException(ErrorCode.BOOKING_MIN_SEATS_REQUIRED);
        }
    }

    /**
     * Sau khi persist booking thành công:
     * Gửi "group invoice" — Phase 3: chỉ log console.
     * Phase 5: thay bằng EmailService.sendGroupInvoice(booking).
     */
    @Override
    protected void postProcess(Booking booking, BookingRequest request) {
        log.info("[GroupBookingFlow] Booking {} đã tạo thành công. " +
                "TODO Phase 5: gửi group invoice email.", booking.getId());
    }
}
