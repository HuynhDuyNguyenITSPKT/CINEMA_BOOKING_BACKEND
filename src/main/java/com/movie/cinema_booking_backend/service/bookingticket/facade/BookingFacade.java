package com.movie.cinema_booking_backend.service.bookingticket.facade;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.ShowtimeRepository;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.response.BookingInitiateResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.bookingticket.proxy.SeatValidationProxy;
import com.movie.cinema_booking_backend.service.bookingticket.singleton.SeatLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.cinema_booking_backend.response.BookingResponse;

import java.time.Duration;
import java.time.Instant;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: FACADE
 * ════════════════════════════════════════════════════════════
 *
 * Facade này cung cấp một API cấp cao duy nhất để Controller gọi.
 * Nó điều phối (orchestrates) 4 hệ thống con phức tạp:
 *  1. Proxy (SeatValidationProxy): Validate ghế trống / chưa mua.
 *  2. Singleton (SeatLockRegistry): Khoá ghế 10 phút.
 *  3. Template+Builder (IBookingService): Tạo Booking Draft.
 *  4. Adapter+Proxy (IPayment): Giao tiếp module Payment của team khác để lấy URL.
 */
@Service
public class BookingFacade {

    private final SeatValidationProxy seatValidationProxy;
    private final SeatLockRegistry seatLockRegistry;
    private final IBookingService bookingService;
    private final IPayment paymentProxy; // Bean IPayment từ teammates
    private final ShowtimeRepository showtimeRepository;

    public BookingFacade(SeatValidationProxy seatValidationProxy,
                         SeatLockRegistry seatLockRegistry,
                         IBookingService bookingService,
                         @org.springframework.beans.factory.annotation.Qualifier("paymentProxy") IPayment paymentProxy,
                         ShowtimeRepository showtimeRepository) {
        this.seatValidationProxy = seatValidationProxy;
        this.seatLockRegistry = seatLockRegistry;
        this.bookingService = bookingService;
        this.paymentProxy = paymentProxy;
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Bắt đầu luồng đặt vé toàn diện.
     * Transactional: Nếu lấy URL MoMo/VNPay lỗi -> rollback việc tạo DB Booking.
     */
    @Transactional
    public BookingInitiateResponse initiateBooking(BookingRequest request, String username) {
        
        // 1. PROXY: Validate 3 tầng (tồn tại, RAM lock, DB status)
        seatValidationProxy.validateForBooking(request.getShowtimeId(), request.getSeatIds(), username);

        // 2. SINGLETON: Khoá ghế trong bộ nhớ (TTL = 10 phút)
        seatLockRegistry.tryLockAll(request.getShowtimeId(), request.getSeatIds(), username, Duration.ofMinutes(10));

        // Note: Lấy Showtime để tạo thông tin label payment
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        // 3. TEMPLATE METHOD + BUILDER: Tạo Booking Draft & Tickets vào DB
        // BookingService sẽ lưu state PENDING / PROCESSING
        BookingResponse bookingDraft = bookingService.createBooking(request, username);

        // 4. IPAYMENT: Gọi dịch vụ Payment của teammates
        PaymentRequest payReq = PaymentRequest.builder()
                .bookingId(bookingDraft.getId())
                .amount(bookingDraft.getTotalAmount().longValue())
                .description("Mua ve xem phim " + showtime.getMovie().getTitle())
                .build();
                
        // (VNPAY / MOMO tuỳ ý user)
        String payUrl = paymentProxy.createPaymentUrl(request.getPaymentMethod().name(), payReq);

        return BookingInitiateResponse.builder()
                .bookingId(bookingDraft.getId())
                .paymentUrl(payUrl)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .build();
    }
}
