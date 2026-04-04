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
import com.movie.cinema_booking_backend.service.bookingticket.builder.BookingContextBuilder;
import com.movie.cinema_booking_backend.service.bookingticket.builder.BookingDirector;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BookingServiceImpl — Điểm kết nối giữa Facade và hệ thống Builder/Engine.
 *
 * Luồng tạo Booking:
 *   1. ObjectProvider lấy 1 instance prototype BookingContextBuilder mới (thread-safe).
 *   2. BookingDirector điều phối 5 bước: reset → loadEntities → runPricing → buildEntities → getResult.
 *   3. bookingRepository.save() persist Booking + Tickets + Extras (cascade).
 *
 * Không còn if-else theo bookingType, không còn Template Method.
 * Thêm loại booking mới → chỉ cần Concrete Builder + Policy mới.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    // ObjectProvider để lấy prototype bean − mỗi request 1 instance mới
    private final ObjectProvider<BookingContextBuilder> builderProvider;
    private final BookingDirector   director;
    private final BookingRepository bookingRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Lấy prototype instance mới − KHÔNG share state giữa các request
        BookingContextBuilder builder = builderProvider.getObject();

        // Director điều phối toàn bộ luồng Builder + PricingEngine
        Booking booking = director.construct(builder, request, username);

        // Persist Booking + cascade save Tickets + BookingExtras
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    // ═══════════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════════

    @Override
    public BookingResponse getBookingById(String bookingId, String username) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

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

    // ═══════════════════════════════════════════════════════════
    //  CANCEL
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingId, String username) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        if (booking.getStatus() == BookingStatus.SUCCESS) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.getTickets().forEach(t -> t.setStatus(TicketStatus.CANCELLED));
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════

    private boolean bookingBelongsToUser(Booking booking, String username) {
        return booking.getUser() != null
                && booking.getUser().getAccount() != null
                && username.equals(booking.getUser().getAccount().getUsername());
    }

    private BookingResponse toResponse(Booking booking) {
        // Lấy thông tin Showtime từ vé đầu tiên (tất cả vé của 1 booking cùng showtime)
        Ticket first     = booking.getTickets().isEmpty() ? null : booking.getTickets().get(0);
        var    showtime  = first != null ? first.getShowtime()  : null;
        var    movie     = showtime != null ? showtime.getMovie()      : null;
        var    auditorium = showtime != null ? showtime.getAuditorium() : null;

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
                .showtimeId(showtime  != null ? showtime.getId()           : null)
                .movieName(movie      != null ? movie.getTitle()           : null)
                .auditoriumName(auditorium != null ? auditorium.getName()  : null)
                .startTime(showtime   != null ? showtime.getStartTime()    : null)
                .tickets(tickets)
                .extras(extras)
                .build();
    }
}
