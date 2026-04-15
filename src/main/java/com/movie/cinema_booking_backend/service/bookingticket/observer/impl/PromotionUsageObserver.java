package com.movie.cinema_booking_backend.service.bookingticket.observer.impl;

import com.movie.cinema_booking_backend.service.IPromotionService;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingPaymentSubject;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingSuccessEvent;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingObserver;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionUsageObserver implements IBookingObserver {

    private final IPromotionService promotionService;

    @Override
    public void update(IBookingSubject subject) {
        if (subject instanceof BookingPaymentSubject concreteSubject) {
            BookingSuccessEvent event = concreteSubject.getState();
            
            if (event.promotionCode() == null || event.promotionCode().isBlank()) {
                log.info("[PromotionUsageObserver] Booking {} khong dung khuyen mai, bo qua tru usage.", event.bookingId());
                return;
            }

            log.info("[PromotionUsageObserver] Thanh toán thành công. Chuẩn bị trừ 1 lần dùng cho code: {}", event.promotionCode());
            try {
                boolean updated = promotionService.decrementUsage(event.promotionCode());
                if (updated) {
                    log.info("[PromotionUsageObserver] Da tru usage thanh cong cho code {}", event.promotionCode());
                } else {
                    log.warn("[PromotionUsageObserver] Khong tru duoc usage cho code {} (het luot/khong ton tai/khong active)", event.promotionCode());
                }
            } catch (Exception e) {
                log.error("[PromotionUsageObserver] Lỗi khi trừ mã khuyến mãi: {}", e.getMessage());
            }
        }
    }
}
