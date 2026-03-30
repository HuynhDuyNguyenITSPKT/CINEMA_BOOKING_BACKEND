package com.movie.cinema_booking_backend.service.bookingticket.template;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.response.BookingExtraResponse;
import com.movie.cinema_booking_backend.response.BookingResponse;
import com.movie.cinema_booking_backend.response.TicketResponse;
import com.movie.cinema_booking_backend.service.bookingticket.builder.BookingDraftBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: TEMPLATE METHOD
 * ════════════════════════════════════════════════════════════
 *
 * Abstract class định nghĩa "bộ khung" (skeleton) của luồng tạo booking.
 * Các bước cố định: validate → reserve → calculate → persist → postProcess
 *
 * Bước có thể override (hook):
 *   - validate():      Subclass có thể thêm rule riêng (VD: Group kiểm min seats)
 *   - postProcess():   Subclass thêm hành động sau persist (VD: Group gửi invoice)
 *
 * Tại sao abstract class thay vì interface?
 *   - Template Method cần inheritance (subclass gọi super.execute() hoặc
 *     abstract class gọi hook của subclass).
 *   - Interface default method không có trạng thái (repositories, etc).
 *   - Abstract class có thể inject repositories qua constructor từ subclass.
 */
public abstract class BookingFlowTemplate {

    protected final BookingRepository    bookingRepository;
    protected final SeatRepository       seatRepository;
    protected final ShowtimeRepository   showtimeRepository;
    protected final ExtraServiceRepository extraServiceRepository;
    protected final PromotionRepository  promotionRepository;
    protected final AccountRepository   accountRepository;

    protected BookingFlowTemplate(BookingRepository bookingRepository,
                                   SeatRepository seatRepository,
                                   ShowtimeRepository showtimeRepository,
                                   ExtraServiceRepository extraServiceRepository,
                                   PromotionRepository promotionRepository,
                                   AccountRepository accountRepository) {
        this.bookingRepository      = bookingRepository;
        this.seatRepository         = seatRepository;
        this.showtimeRepository     = showtimeRepository;
        this.extraServiceRepository = extraServiceRepository;
        this.promotionRepository    = promotionRepository;
        this.accountRepository      = accountRepository;
    }

    // ════════════════════════════════════════
    //  TEMPLATE METHOD — bộ khung cố định
    // ════════════════════════════════════════

    public final BookingResponse execute(BookingRequest request, String username) {

        // Step 1: Load & validate entities
        Showtime showtime = loadShowtime(request.getShowtimeId());
        User user = loadUser(username);
        List<Seat> seats = loadSeats(request.getSeatIds(), showtime);
        Promotion promotion = loadPromotion(request.getPromotionCode());

        // Step 2: Hook — subclass có thể validate thêm rule riêng
        validate(request, seats, showtime, user);

        // Step 3: Build draft Booking (Builder Pattern gọi ở đây)
        BookingDraftBuilder builder = new BookingDraftBuilder()
                .user(user)
                .showtime(showtime)
                .addSeats(seats)
                .promotion(promotion)
                .note(request.getNote());

        if (request.getExtras() != null && !request.getExtras().isEmpty()) {
            for (Map.Entry<Long, Integer> entry : request.getExtras().entrySet()) {
                ExtraService extra = extraServiceRepository.findById(entry.getKey())
                        .orElseThrow(() -> new AppException(ErrorCode.EXTRA_SERVICE_NOT_FOUND));
                builder.addExtra(extra, entry.getValue());
            }
        }

        Booking booking = builder.build();

        // Step 4: Persist (cascade saves Tickets + BookingExtras)
        bookingRepository.save(booking);

        // Step 5: Hook — hành động sau persist (gửi email, invoice group...)
        postProcess(booking, request);

        return toResponse(booking, showtime);
    }

    // ════════════════════════════════════════
    //  HOOKS — subclass override nếu cần
    // ════════════════════════════════════════

    /**
     * Validate thêm rule riêng của từng flow.
     * StandardBookingFlow: không override (dùng default no-op).
     * GroupBookingFlow:   override để kiểm tra số ghế tối thiểu.
     */
    protected void validate(BookingRequest request, List<Seat> seats,
                            Showtime showtime, User user) {
        // default: no-op
    }

    /**
     * Hành động sau khi đã persist booking thành công.
     * StandardBookingFlow: không cần gì thêm.
     * GroupBookingFlow:   có thể gửi group invoice email (mock ở Phase 3).
     */
    protected void postProcess(Booking booking, BookingRequest request) {
        // default: no-op
    }

    // ════════════════════════════════════════
    //  Private — các bước cố định
    // ════════════════════════════════════════

    private Showtime loadShowtime(String showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
    }

    private User loadUser(String username) {
        // User entity được link qua Account.username
        return accountRepository.findByUsername(username)
                .map(Account::getUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private List<Seat> loadSeats(List<String> seatIds, Showtime showtime) {
        List<Seat> seats = new ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
            // Đảm bảo ghế thuộc đúng auditorium của showtime
            if (!seat.getAuditorium().getId().equals(showtime.getAuditorium().getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seats.add(seat);
        }
        return seats;
    }

    private Promotion loadPromotion(String code) {
        if (code == null || code.isBlank()) return null;
        return promotionRepository.getPromotionByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }

    protected BookingResponse toResponse(Booking booking, Showtime showtime) {
        List<TicketResponse> ticketResponses = booking.getTickets().stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .seatId(t.getSeat().getId())
                        .seatName(t.getSeat().getName())
                        .seatTypeName(t.getSeat().getSeatType() != null
                                ? t.getSeat().getSeatType().getName() : null)
                        .price(t.getPrice())
                        .status(t.getStatus())
                        .qrCodeUrl(t.getQrCodeUrl())
                        .build())
                .collect(Collectors.toList());

        List<BookingExtraResponse> extraResponses = booking.getBookingExtras().stream()
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
                .showtimeId(showtime != null ? showtime.getId() : null)
                .movieName(showtime != null && showtime.getMovie() != null
                        ? showtime.getMovie().getTitle() : null)
                .auditoriumName(showtime != null && showtime.getAuditorium() != null
                        ? showtime.getAuditorium().getName() : null)
                .startTime(showtime != null ? showtime.getStartTime() : null)
                .tickets(ticketResponses)
                .extras(extraResponses)
                .build();
    }
}
