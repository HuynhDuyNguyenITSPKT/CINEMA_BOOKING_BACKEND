package com.movie.cinema_booking_backend.service.bookingticket.facade;

import com.movie.cinema_booking_backend.config.SeatLockProperties;
import com.movie.cinema_booking_backend.response.TicketResponse;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.response.BookingInitiateResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.ISeatLockService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.cinema_booking_backend.response.BookingResponse;

import java.time.Instant;

@Service
public class BookingFacade {

    private final ISeatLockService seatLockService;
    private final IBookingService bookingService;
    private final IPayment paymentProxy;
    private final SeatLockProperties seatLockProperties;

    public BookingFacade(ISeatLockService seatLockService,
                         IBookingService bookingService,
                         @Qualifier("paymentProxy") IPayment paymentProxy,
                         SeatLockProperties seatLockProperties) {
        this.seatLockService = seatLockService;
        this.bookingService = bookingService;
        this.paymentProxy = paymentProxy;
        this.seatLockProperties = seatLockProperties;
    }
    @Transactional
    public BookingInitiateResponse initiateBooking(BookingRequest request, String username) {
        seatLockService.validateForBooking(request.getShowtimeId(), request.getSeatIds(), username);
        seatLockService.lockSeats(request.getShowtimeId(), request.getSeatIds(), username,
                seatLockProperties.getTtlDuration());
        try {
            BookingResponse bookingDraft = bookingService.createBooking(request, username);
            String payUrl = "";
            if (bookingDraft.getStatus() != com.movie.cinema_booking_backend.enums.BookingStatus.PENDING_APPROVAL) {
                PaymentRequest payReq = PaymentRequest.builder()
                        .bookingId(bookingDraft.getId())
                        .amount(bookingDraft.getGrandTotalPrice().longValue())
                        .description("Mua ve xem phim " + (bookingDraft.getMovieName() != null ? bookingDraft.getMovieName() : ""))
                        .build();
                payUrl = paymentProxy.createPaymentUrl(request.getPaymentMethod().name(), payReq);
            }

            return BookingInitiateResponse.builder()
                    .bookingId(bookingDraft.getId())
                    .paymentUrl(payUrl)
                    .expiresAt(Instant.now().plus(seatLockProperties.getTtlDuration()))
                    .build();
        } catch (RuntimeException ex) {
            seatLockService.unlockSeats(request.getShowtimeId(), request.getSeatIds(), username);
            throw ex;
        }
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingId, String username) {
        BookingResponse response = bookingService.cancelBooking(bookingId, username);
        if (response.getShowtimeId() != null && response.getTickets() != null) {
            java.util.List<String> seatIds = response.getTickets().stream()
                    .map(TicketResponse::getSeatId)
                    .toList();
            seatLockService.unlockSeats(response.getShowtimeId(), seatIds, username);
        }
        return response;
    }

    @Transactional
    public BookingResponse approveBooking(String bookingId) {
        BookingResponse response = bookingService.approveBooking(bookingId);
        return response;
    }

    @Transactional
    public BookingResponse adminCancelBooking(String bookingId) {
        BookingResponse response = bookingService.adminCancelBooking(bookingId);

        if (response.getShowtimeId() != null && response.getTickets() != null) {
            java.util.List<String> seatIds = response.getTickets().stream()
                    .map(TicketResponse::getSeatId)
                    .toList();
        }
        return response;
    }

}
