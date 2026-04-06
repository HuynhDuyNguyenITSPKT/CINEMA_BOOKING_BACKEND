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
        builder.validateRules();             // Step 3: Validate theo từng luật của class con
        builder.runPricing(pricingEngine);   // Step 4: Tính tiền (Pipeline)
        builder.buildEntities();             // Step 5: Đúc Entity
        return builder.getResult();          // Step 6: Lấy sản phẩm
    }

    /**
     * Chạy pipeline CHỈ ĐẾN bước tính giá (Step 4).
     * Không buildEntities(), không persist — dùng cho Preview Price API.
     * Caller đọc calcResult từ AbstractBookingBuilder sau khi gọi.
     */
    public void constructPreview(BookingBuilder builder, BookingRequest request, String username) {
        builder.reset(request, username);  // Step 1
        builder.loadEntities();            // Step 2
        builder.validateRules();           // Step 3 (validate promotion, seat count...)
        builder.runPricing(pricingEngine); // Step 4 — Chain of Responsibility chạy xong
        // Step 5 & 6 bị bỏ qua: Không tạo entity, không lưu DB
    }
}
