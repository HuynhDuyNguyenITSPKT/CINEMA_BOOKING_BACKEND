package com.movie.cinema_booking_backend.service.bookingticket.engine;

import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: CHAIN OF RESPONSIBILITY (GoF - Engine)
 * ═══════════════════════════════════════════════════════════
 *
 * Spring tự động inject TẤT CẢ PricingStep beans theo @Order:
 *   Order(1) BasePriceStep   → Order(2) SurchargeStep
 *   Order(3) PromotionStep   → Order(4) TaxStep
 *   Order(5) ExtrasStep
 *
 * Thêm trạm mới chỉ cần tạo class @Component + @Order(n).
 * Engine này không cần sửa (Open/Closed Principle).
 *
 * Engine là Singleton Bean vì nó KHÔNG giữ state – state nằm
 * trong CalculationResult được tạo mới mỗi lần tính.
 */
@Component
@RequiredArgsConstructor
public class PricingEngine {

    // Spring inject tự động tất cả PricingStep beans, sắp xếp theo @Order
    private final List<PricingStep> steps;

    /**
     * Chạy toàn bộ Pipeline và trả về kết quả tính giá.
     *
     * @param request Đầu vào immutable từ Builder
     * @return CalculationResult chứa đầy đủ breakdown và finalTotal
     */
    public CalculationResult calculate(CalculationRequest request) {
        CalculationResult result = new CalculationResult();
        for (PricingStep step : steps) {
            step.process(request, result);
        }
        return result;
    }
}
