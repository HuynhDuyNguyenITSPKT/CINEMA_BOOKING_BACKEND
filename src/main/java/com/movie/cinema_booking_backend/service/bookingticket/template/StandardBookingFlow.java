package com.movie.cinema_booking_backend.service.bookingticket.template;

import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * StandardBookingFlow — Luồng đặt vé thông thường (1-4 ghế).
 *
 * Không override validate() hay postProcess() → dùng default no-op.
 * Toàn bộ logic nằm trong BookingFlowTemplate.execute().
 *
 * @Qualifier("standard") → BookingController inject đúng bean này
 *   thay vì GroupBookingFlow.
 */
@Service
@Qualifier("standard")
public class StandardBookingFlow extends BookingFlowTemplate {

    public StandardBookingFlow(BookingRepository bookingRepository,
                               SeatRepository seatRepository,
                               ShowtimeRepository showtimeRepository,
                               ExtraServiceRepository extraServiceRepository,
                               PromotionRepository promotionRepository,
                               AccountRepository accountRepository) {
        super(bookingRepository, seatRepository, showtimeRepository,
              extraServiceRepository, promotionRepository, accountRepository);
    }

    // Không cần override gì cả — kế thừa toàn bộ từ BookingFlowTemplate
}
