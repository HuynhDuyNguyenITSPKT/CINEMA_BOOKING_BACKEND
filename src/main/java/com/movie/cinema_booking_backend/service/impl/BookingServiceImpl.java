package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.AdminUpdateGroupSeatsRequest;
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
    private final AccountRepository      accountRepository;
    private final TicketRepository       ticketRepository;
    private final PromotionRepository    promotionRepository;
    private final ExtraServiceRepository extraServiceRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Chuẩn bị context (Lego pieces) thay cho Builder
        BookingContext context = prepareContext(request, username);

        // Factory chọn đúng Builder (Standard, Couple, Group) dựa trên type truyền vào
        BookingBuilder builder = factory.getBuilder(request.getBookingType());

        // Director điều phối toàn bộ luồng
        Booking booking = director.construct(builder, request, context, username);

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
        BookingContext context = prepareContext(request, username);
        BookingBuilder builderRaw = factory.getBuilder(request.getBookingType());
        // Construct chỉ đến bước runPricing (không buildEntities, không save)
        director.constructPreview(builderRaw, request, context, username);

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
     * Admin cập nhật ghế cho đơn B2B và duyệt đơn.
     *
     * Luồng (2-Phase Finalization):
     *  1. Tải đơn PENDING_APPROVAL từ DB.
     *  2. Gọi GroupBookingBuilder (qua Director) để khởi tạo Booking "bản vẽ" mới
     *     với danh sách ghế Admin đã chốt. Builder chạy Pipeline:
     *     GroupDiscountStep(-5%) → TaxStep(+10%) → chia đều giá/ticket.
     *  3. JPA Diffing: So sánh Tickets cũ vs Tickets mới.
     *     - Ticket bị loại: orphanRemoval=true → DELETE → Ghế xanh lại ngay lập tức.
     *     - Ticket mới: addTicket() → INSERT.
     *  4. Chuyển trạng thái sang RESERVED (sẵn sàng thanh toán online).
     *
     * Builder Pattern KHÔNG bị vi phạm: Builder chỉ tạo Object trên RAM.
     * Việc đồng bộ DB là trách nhiệm của Service Layer (tầng Orchestration).
     */
    @Override
    @Transactional
    public BookingResponse updateGroupBookingSeatsAndApprove(String bookingId, AdminUpdateGroupSeatsRequest request) {
        // 1. Load đơn cần duyệt
        Booking dbBooking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (dbBooking.getStatus() != BookingStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 2. Tải Showtime (có thể là showtime mới nếu admin đổi)
        String targetShowtimeId = request.getShowtimeId() != null
                ? request.getShowtimeId()
                : (dbBooking.getTickets().isEmpty() ? null
                    : dbBooking.getTickets().get(0).getShowtime().getId());

        if (targetShowtimeId == null) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        // 3. Chuẩn bị BookingRequest để nạp vào Builder
        BookingRequest syntheticRequest = new BookingRequest();
        syntheticRequest.setShowtimeId(targetShowtimeId);
        syntheticRequest.setSeatIds(request.getSeatIds());
        syntheticRequest.setBookingType("GROUP");
        syntheticRequest.setNote("[GROUP - B2B] " + (request.getAdminNote() != null ? request.getAdminNote() : "Duyệt bởi Admin"));

        // 4. Gọi Builder qua Director — Builder xây Booking mới trên RAM với giá đã tính
        String username = dbBooking.getUser().getAccount().getUsername();
        BookingContext context = prepareContext(syntheticRequest, username);
        BookingBuilder builder = factory.getBuilder("GROUP");
        Booking builtBooking = director.construct(builder, syntheticRequest, context, username);

        // 5. JPA Diffing — Đồng bộ Tickets vào dbBooking (không tạo row mới)
        java.util.Set<String> newSeatIds = new java.util.HashSet<>(request.getSeatIds());

        // Xóa các Ticket không còn trong danh sách mới
        // orphanRemoval = true trên @OneToMany(tickets) sẽ tự DELETE khỏi DB
        dbBooking.getTickets().removeIf(t ->
                t.getSeat() == null || !newSeatIds.contains(t.getSeat().getId()));

        // Cập nhật giá những ghế còn lại, thêm ghế mới nếu cần
        java.util.Map<String, Ticket> builtMap = new java.util.HashMap<>();
        builtBooking.getTickets().forEach(t -> builtMap.put(t.getSeat().getId(), t));

        java.util.Set<String> existingSeatIds = dbBooking.getTickets().stream()
                .filter(t -> t.getSeat() != null)
                .map(t -> t.getSeat().getId())
                .collect(java.util.stream.Collectors.toSet());

        // Update giá các ghế giữ lại
        dbBooking.getTickets().forEach(t -> {
            if (t.getSeat() != null && builtMap.containsKey(t.getSeat().getId())) {
                t.setFinalPrice(builtMap.get(t.getSeat().getId()).getFinalPrice());
                t.setStatus(TicketStatus.PROCESSING);
            }
        });

        // Thêm ghế mới
        builtBooking.getTickets().stream()
                .filter(t -> t.getSeat() != null && !existingSeatIds.contains(t.getSeat().getId()))
                .forEach(t -> {
                    t.setBooking(dbBooking);
                    dbBooking.getTickets().add(t);
                });

        // 6. Chốt tổng tiền và chuyển trạng thái
        dbBooking.setGrandTotalPrice(builtBooking.getGrandTotalPrice());
        dbBooking.setNote(builtBooking.getNote());
        dbBooking.setStatus(BookingStatus.RESERVED);

        bookingRepository.save(dbBooking);
        return toResponse(dbBooking);
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

    @Override
    @Transactional
    public void cancelExpiredReservedBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoffTime);
        
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getTickets().forEach(t -> t.setStatus(TicketStatus.CANCELLED));
        }
        
        if (!expiredBookings.isEmpty()) {
            bookingRepository.saveAll(expiredBookings);
            // Logger có thể được thêm vào đây để ghi log console
            System.out.println("[CronJob] Đã huỷ " + expiredBookings.size() + " đơn đặt vé quá hạn thanh toán.");
        }
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
                        .finalPrice(t.getFinalPrice())
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
                .grandTotalPrice(booking.getGrandTotalPrice())
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

    /**
     * Chuẩn bị dữ liệu (Data Fetching + DB Guard) cho Builder.
     */
    private BookingContext prepareContext(BookingRequest request, String username) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        User user = accountRepository.findByUsername(username)
                .map(Account::getUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Seat> seats = new java.util.ArrayList<>();
        for (String seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
            if (!seat.getAuditorium().getId().equals(showtime.getAuditorium().getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seats.add(seat);
        }

        // ─── DB Guard: chặn double-booking ──────────────────────────────────────
        java.util.Set<String> occupiedSeatIds = ticketRepository.findSeatIdsByShowtimeIdAndStatuses(
                request.getShowtimeId(),
                java.util.EnumSet.of(TicketStatus.PROCESSING, TicketStatus.BOOKED, TicketStatus.USED)
        );
        for (Seat seat : seats) {
            if (occupiedSeatIds.contains(seat.getId())) {
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
        }
        // ────────────────────────────────────────────────────────────────────────

        String code = request.getPromotionCode();
        Promotion promotion = (code != null && !code.isBlank())
                ? promotionRepository.getPromotionByCode(code).orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND))
                : null;

        List<ExtraService> extraServices = new java.util.ArrayList<>();
        if (request.getExtras() != null) {
            for (Long extraId : request.getExtras().keySet()) {
                extraServices.add(extraServiceRepository.findById(extraId)
                        .orElseThrow(() -> new AppException(ErrorCode.EXTRA_SERVICE_NOT_FOUND)));
            }
        }

        return new BookingContext(showtime, user, seats, promotion, extraServices);
    }
}
