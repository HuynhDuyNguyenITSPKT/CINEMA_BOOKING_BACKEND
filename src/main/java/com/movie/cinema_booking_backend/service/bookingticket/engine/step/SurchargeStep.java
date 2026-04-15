package com.movie.cinema_booking_backend.service.bookingticket.engine.step;

import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingStep;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ─── Trạm 2: Surcharge ────────────────────────────────────
 * Tính riêng phụ thu ghế (VIP, Sweetbox, 3D...).
 *
 * ╔══════════════════════════════════════════════════════════╗
 * ║ Surcharge được lưu riêng trong surchargesTotal.         ║
 * ║ PromotionStep gộp cả base + surcharges làm discount     ║
 * ║ base, nhất quán với giá "Tiền Vé" hiển thị trên UI.    ║
 * ╚══════════════════════════════════════════════════════════╝
 *
 * Đồng thời khởi tạo giá tạm của từng vé = base + surcharge
 * (PromotionStep sẽ điều chỉnh sau).
 */
@Component
@Order(2)
public class SurchargeStep implements PricingStep {

    @Override
    public void process(CalculationRequest request, CalculationResult result) {
        BigDecimal base       = request.baseTicketPrice();
        BigDecimal surchTotal = BigDecimal.ZERO;

        for (CalculationRequest.SeatInfo seat : request.seats()) {
            BigDecimal surcharge = seat.surchargeAmount();
            surchTotal = surchTotal.add(surcharge);

            // Giá tạm = base + surcharge (PromotionStep sẽ trừ discount trên tổng này)
            BigDecimal rawPrice = base.add(surcharge).setScale(2, RoundingMode.HALF_UP);
            result.putTicketPrice(seat.seatId(), rawPrice);
        }

        result.setSurchargesTotal(surchTotal);
    }
}
