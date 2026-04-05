package com.movie.cinema_booking_backend.service.bookingticket.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: SIMPLE FACTORY
 * ═══════════════════════════════════════════════════════════
 *
 * Nhiệm vụ duy nhất: Chọn đúng đồ nghề (Concrete Builder)
 * dựa trên bookingType truyền vào từ Client Request.
 *
 * Dùng ApplicationContext.getBean() thay vì inject bằng DI
 * để luôn lấy được MỘT INSTANCE MỚI (@Scope Prototype) mỗi lần gọi.
 */
@Component
@RequiredArgsConstructor
public class BookingBuilderFactory {

    private final ApplicationContext applicationContext;

    public BookingBuilder getBuilder(String bookingType) {
        if (bookingType == null) {
            return applicationContext.getBean(StandardBookingBuilder.class);
        }

        return switch (bookingType.toUpperCase()) {
            case "COUPLE" -> applicationContext.getBean(CoupleBookingBuilder.class);
            case "GROUP"  -> applicationContext.getBean(GroupBookingBuilder.class);
            default       -> applicationContext.getBean(StandardBookingBuilder.class);
        };
    }
}
