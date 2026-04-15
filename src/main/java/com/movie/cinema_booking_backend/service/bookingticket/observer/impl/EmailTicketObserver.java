package com.movie.cinema_booking_backend.service.bookingticket.observer.impl;

import com.movie.cinema_booking_backend.service.IEmailService;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingPaymentSubject;
import com.movie.cinema_booking_backend.service.bookingticket.observer.BookingSuccessEvent;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingObserver;
import com.movie.cinema_booking_backend.service.bookingticket.observer.IBookingSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTicketObserver implements IBookingObserver {

    private final IEmailService emailService;

    @Override
    @Async
    public void update(IBookingSubject subject) {
        if (subject instanceof BookingPaymentSubject concreteSubject) {
            BookingSuccessEvent event = concreteSubject.getState();
            if (event.userEmail() == null) {
                log.warn("[EmailTicketObserver] Khách hàng chưa có Email, bỏ qua.");
                return;
            }
            if (event.ticketIds() == null || event.ticketIds().isEmpty()) {
                log.warn("[EmailTicketObserver] Booking {} khong co ticketId de tao QR.", event.bookingId());
                return;
            }
            log.info("[EmailTicketObserver] Bắt đầu gửi Email chứa mã QR vé tới: {}", event.userEmail());
            try {
                emailService.sendTicketQrEmail(
                        event.userEmail(),
                        event.bookingId(),
                        event.movieName(),
                        event.grandTotal() != null ? event.grandTotal().toPlainString() : "0",
                        event.ticketIds()
                );
                log.info("[EmailTicketObserver] Đã gửi QR code thành công tới: {}", event.userEmail());
            } catch (Exception e) {
                log.error("[EmailTicketObserver] Lỗi khi gửi QR tới {}: {}", event.userEmail(), e.getMessage());
            }
        }
    }
}
