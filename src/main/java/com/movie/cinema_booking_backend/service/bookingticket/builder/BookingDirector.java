package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: DIRECTOR (GoF)
 * ═══════════════════════════════════════════════════════════
 *
 * Director biết THỨ TỰ các bước cần thực hiện, nhưng KHÔNG
 * biết Builder cụ thể là loại gì. Nó chỉ làm việc với interface
 * BookingBuilder – đây là bản chất của Dependency Inversion.
 *
 * Khi muốn thêm loại booking mới (CooperationBooking):
 *   → Tạo 1 Concrete Builder mới implement BookingBuilder
 *   → Director không cần sửa gì cả
 *
 * Khác với Template Method:
 *   Template: Subclass quyết định "làm gì" trong mỗi bước.
 *   Director: Không có inheritance. Director gọi Builder qua
 *   interface, hoàn toàn không biết implementation bên trong.
 */
@Component
@RequiredArgsConstructor
public class BookingDirector {

    private final PricingEngine pricingEngine;

    /**
     * Điều phối 5 bước xây dựng Booking theo đúng thứ tự GoF.
     *
     * @param builder  Concrete Builder (prototype instance, thread-safe)
     * @param request  Request từ client
     * @param username Username đang đặt vé
     * @return Booking entity đã hoàn thiện (chưa persist)
     */
    public Booking construct(BookingBuilder builder, BookingRequest request, String username) {
        builder.reset(request, username);    // Step 1: Nhận input, clean state
        builder.loadEntities();              // Step 2: Load từ DB
        builder.runPricing(pricingEngine);   // Step 3: Tính tiền (Pipeline)
        builder.buildEntities();             // Step 4: Đúc Entity
        return builder.getResult();          // Step 5: Lấy sản phẩm
    }
}
