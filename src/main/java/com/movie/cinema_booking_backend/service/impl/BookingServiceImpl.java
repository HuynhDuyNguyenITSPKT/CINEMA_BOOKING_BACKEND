package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.BookingRepository;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.response.BookingExtraResponse;
import com.movie.cinema_booking_backend.response.BookingResponse;
import com.movie.cinema_booking_backend.response.TicketResponse;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.bookingticket.template.GroupBookingFlow;
import com.movie.cinema_booking_backend.service.bookingticket.template.StandardBookingFlow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService {

    private final StandardBookingFlow standardBookingFlow;
    private final GroupBookingFlow    groupBookingFlow;
    private final BookingRepository   bookingRepository;

    public BookingServiceImpl(StandardBookingFlow standardBookingFlow,
                              GroupBookingFlow groupBookingFlow,
                              BookingRepository bookingRepository) {
        this.standardBookingFlow = standardBookingFlow;
        this.groupBookingFlow    = groupBookingFlow;
        this.bookingRepository   = bookingRepository;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Chọn Template Method flow dựa vào bookingType trong request
        if ("GROUP".equalsIgnoreCase(request.getBookingType())) {
            return groupBookingFlow.execute(request, username);
        }
        return standardBookingFlow.execute(request, username);
    }

    @Override
    public BookingResponse getBookingById(String bookingId, String username) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra ownership: user chỉ xem booking của chính mình
        if (!bookingBelongsToUser(booking, username)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return toResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(String username) {
        return bookingRepository.findByUser_Account_Username(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingId, String username) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        if (booking.getStatus() == BookingStatus.SUCCESS) {
            // Đã thanh toán → không cancel đơn giản (cần refund flow — Phase 4)
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        // Cancel booking + tất cả tickets về CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getTickets().forEach(t -> t.setStatus(TicketStatus.CANCELLED));
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private boolean bookingBelongsToUser(Booking booking, String username) {
        return booking.getUser() != null
                && booking.getUser().getAccount() != null
                && username.equals(booking.getUser().getAccount().getUsername());
    }

    private BookingResponse toResponse(Booking booking) {
        List<TicketResponse> tickets = booking.getTickets().stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .seatId(t.getSeat() != null ? t.getSeat().getId() : null)
                        .seatName(t.getSeat() != null ? t.getSeat().getName() : null)
                        .seatTypeName(t.getSeat() != null && t.getSeat().getSeatType() != null
                                ? t.getSeat().getSeatType().getName() : null)
                        .price(t.getPrice())
                        .status(t.getStatus())
                        .qrCodeUrl(t.getQrCodeUrl())
                        .build())
                .collect(Collectors.toList());

        List<BookingExtraResponse> extras = booking.getBookingExtras().stream()
                .map(e -> BookingExtraResponse.builder()
                        .extraServiceId(e.getExtraService().getId())
                        .extraServiceName(e.getExtraService().getName())
                        .quantity(e.getQuantity())
                        .totalPrice(e.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getCreatedAt())
                .note(booking.getNote())
                .tickets(tickets)
                .extras(extras)
                .build();
    }
}
