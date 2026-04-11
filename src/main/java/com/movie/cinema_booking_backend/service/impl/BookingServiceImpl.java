package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.AdminBookingRequest;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.response.*;
import com.movie.cinema_booking_backend.service.IBookingService;
import com.movie.cinema_booking_backend.service.bookingticket.builder.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BookingServiceImpl — Điểm kết nối giữa Facade và hệ thống Builder/Engine.
 *
 * Luồng tạo Booking:
 *   1. BookingBuilderFactory lấy 1 instance prototype Builder tương ứng (thread-safe).
 *   2. BookingDirector điều phối 6 bước: reset → loadEntities → validateRules → runPricing → buildEntities → getResult.
 *   3. bookingRepository.save() persist Booking + Tickets + Extras (cascade).
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingBuilderFactory factory;
    private final BookingDirector       director;
    private final BookingRepository     bookingRepository;
    private final SeatRepository        seatRepository;
    private final ShowtimeRepository    showtimeRepository;
    private final AccountRepository     accountRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Factory chọn đúng Builder (Standard, Couple, Group) dựa trên type truyền vào
        BookingBuilder builder = factory.getBuilder(request.getBookingType());

        // Director điều phối toàn bộ luồng
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
            throw new AppException(ErrorCode.BOOKING_ALREADY_PAID);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.getTickets().forEach(t -> t.setStatus(TicketStatus.CANCELLED));
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    // ═══════════════════════════════════════════════════════════
    //  PREVIEW PRICE (No DB write — Feed Chain of Responsibility only)
    // ═══════════════════════════════════════════════════════════

    /**
     * Chạy toàn bộ PricingEngine qua Builder nhưng KHÔNG gọi save().
     * Frontend dùng để hiển thị hóa đơn realtime tại Checkout.
     */
    @Override
    @Transactional(readOnly = true)
    public PricePreviewResponse calculatePreviewPrice(BookingRequest request, String username) {
        BookingBuilder builderRaw = factory.getBuilder(request.getBookingType());
        // Construct chỉ đến bước runPricing (không buildEntities, không save)
        director.constructPreview(builderRaw, request, username);

        AbstractBookingBuilder builder = (AbstractBookingBuilder) builderRaw;
        var calcResult = builder.getCalcResult();
        boolean isGroup = "GROUP".equalsIgnoreCase(request.getBookingType());

        return PricePreviewResponse.builder()
                .baseSubtotal(calcResult.getBaseSubtotal())
                .surchargesTotal(calcResult.getSurchargesTotal())
                .promotionDiscount(calcResult.getPromotionDiscount())
                .extrasTotal(calcResult.getExtrasTotal())
                .taxAmount(calcResult.getTaxAmount())
                .finalTotal(calcResult.getFinalTotal())
                .isGroupBooking(isGroup)
                .promotionDescription(isGroup ? "Chiết khấu Khách Đoàn -5%" :
                        (request.getPromotionCode() != null ? "Voucher: " + request.getPromotionCode() : null))
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    public List<BookingResponse> getAllBookings(String status) {
        List<Booking> bookings;
        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus;
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            bookings = bookingRepository.findByStatus(bookingStatus);
        } else {
            bookings = bookingRepository.findAll();
        }
        return bookings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Admin tạo booking ngoại lệ:
     * - Không qua validateRules() (bypass max seat limit)
     * - Nếu có manualTotalAmount thì override giá hệ thống
     */
    @Override
    @Transactional
    public BookingResponse adminCreateBooking(AdminBookingRequest request) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        Long userId;
        try {
            userId = Long.valueOf(request.getUserId());
        } catch (NumberFormatException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User user = accountRepository.findByUserId(userId)
                .map(Account::getUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Seat> seats = request.getSeatIds().stream()
                .map(id -> seatRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND)))
                .collect(Collectors.toList());

        // Dùng giá override nếu Admin chỉ định, nếu không tính tự động theo basePrice
        BigDecimal total = request.getManualTotalAmount() != null
                ? request.getManualTotalAmount()
                : BigDecimal.valueOf(showtime.getBasePrice()).multiply(BigDecimal.valueOf(seats.size()));

        Booking booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.RESERVED)
                .totalAmount(total)
                .createdAt(LocalDateTime.now())
                .note("[ADMIN OVERRIDE] " + (request.getNote() != null ? request.getNote() : ""))
                .build();

        for (Seat seat : seats) {
            BigDecimal seatPrice = total.divide(BigDecimal.valueOf(seats.size()), 2, RoundingMode.HALF_UP);
            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime).seat(seat).price(seatPrice)
                    .status(TicketStatus.PROCESSING)
                    .build();
            booking.addTicket(ticket);
        }

        bookingRepository.save(booking);
        return toResponse(booking);
    }

    /**
     * Duyệt đơn B2B: Chỉ chấp nhận PENDING_APPROVAL -> RESERVED.
     * Từ chối thẳng tay nếu status không phải PENDING_APPROVAL.
     */
    @Override
    @Transactional
    public BookingResponse approveBooking(String bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        booking.setStatus(BookingStatus.RESERVED);
        booking.getTickets().forEach(t -> t.setStatus(TicketStatus.PROCESSING));
        bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse adminCancelBooking(String bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        if (booking.getStatus() == BookingStatus.SUCCESS) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_PAID);
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
        Ticket first      = booking.getTickets().isEmpty() ? null : booking.getTickets().get(0);
        var    showtime   = first != null ? first.getShowtime()         : null;
        var    movie      = showtime != null ? showtime.getMovie()      : null;
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
                .showtimeId(showtime  != null ? showtime.getId()          : null)
                .movieName(movie      != null ? movie.getTitle()          : null)
                .auditoriumName(auditorium != null ? auditorium.getName() : null)
                .startTime(showtime   != null ? showtime.getStartTime()   : null)
                .tickets(tickets)
                .extras(extras)
                .build();
    }
}
