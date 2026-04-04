package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.ExtraLineItem;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.SeatInfo;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;
import com.movie.cinema_booking_backend.service.bookingticket.engine.policy.PolicyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: BUILDER (GoF - Concrete Builder)
 * ═══════════════════════════════════════════════════════════
 *
 * @Scope("prototype") — THIẾT YẾU:
 *   Builder lưu state trung gian (seats, user, showtime...).
 *   Nếu là Singleton, 2 request đồng thời sẽ ghi đè state
 *   của nhau → race condition, sai giá vé (lỗi chí mạng).
 *   Prototype đảm bảo mỗi request có instance riêng.
 *
 * Trách nhiệm duy nhất: Thu thập dữ liệu từ DB và lắp ráp
 * thành Entity. KHÔNG làm bất kỳ phép tính nào.
 * Toán học là trách nhiệm 100% của PricingEngine.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class BookingContextBuilder implements BookingBuilder {

    // ─── Repositories ─────────────────────────────────────────────────────────
    private final BookingRepository    bookingRepo;
    private final SeatRepository       seatRepo;
    private final ShowtimeRepository   showtimeRepo;
    private final ExtraServiceRepository extraRepo;
    private final PromotionRepository  promoRepo;
    private final AccountRepository    accountRepo;
    private final PolicyFactory        policyFactory;

    // ─── State (reset mỗi lần dùng) ───────────────────────────────────────────
    private BookingRequest  request;
    private String          username;

    // ─── Entities đã load ────────────────────────────────────────────────────
    private User            user;
    private Showtime        showtime;
    private List<Seat>      seats;
    private Promotion       promotion;   // nullable
    private List<ExtraService> extraServices;

    // ─── Kết quả từ Engine ────────────────────────────────────────────────────
    private CalculationResult calcResult;

    // ─── Sản phẩm cuối ────────────────────────────────────────────────────────
    private Booking booking;

    // ═══════════════════════════════════════════════════════════
    //  Step 1 – reset: Nhận input, xoá state cũ
    // ═══════════════════════════════════════════════════════════
    @Override
    public void reset(BookingRequest request, String username) {
        this.request      = request;
        this.username     = username;
        this.booking      = null;
        this.calcResult   = null;
    }

    // ═══════════════════════════════════════════════════════════
    //  Step 2 – loadEntities: Chỉ nói chuyện với DB
    // ═══════════════════════════════════════════════════════════
    @Override
    public void loadEntities() {
        // Load Showtime
        this.showtime = showtimeRepo.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        // Load User qua Account
        this.user = accountRepo.findByUsername(username)
                .map(Account::getUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Load Seats & validate thuộc đúng auditorium
        this.seats = new ArrayList<>();
        for (String seatId : request.getSeatIds()) {
            Seat seat = seatRepo.findById(seatId)
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
            if (!seat.getAuditorium().getId().equals(showtime.getAuditorium().getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seats.add(seat);
        }

        // Validate nghiệp vụ (min/max seats) theo Policy – fail-fast trước khi tính tiền
        policyFactory.getPolicy(request.getBookingType())
                .validateEligibility(buildCalculationRequest());

        // Load Promotion (nullable)
        String code = request.getPromotionCode();
        this.promotion = (code != null && !code.isBlank())
                ? promoRepo.getPromotionByCode(code)
                        .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND))
                : null;

        // Load Extra Services
        this.extraServices = new ArrayList<>();
        if (request.getExtras() != null) {
            for (Long extraId : request.getExtras().keySet()) {
                extraServices.add(extraRepo.findById(extraId)
                        .orElseThrow(() -> new AppException(ErrorCode.EXTRA_SERVICE_NOT_FOUND)));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Step 3 – runPricing: Uỷ quyền toàn bộ tính toán cho Engine
    // ═══════════════════════════════════════════════════════════
    @Override
    public void runPricing(PricingEngine engine) {
        this.calcResult = engine.calculate(buildCalculationRequest());
    }

    // ═══════════════════════════════════════════════════════════
    //  Step 4 – buildEntities: Đúc Entity từ CalculationResult
    // ═══════════════════════════════════════════════════════════
    @Override
    public void buildEntities() {
        // Tạo Booking với totalAmount từ Engine (chưa persist)
        this.booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(BookingStatus.PENDING)
                .totalAmount(calcResult.getFinalTotal())
                .createdAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        // Tạo Ticket, giá lấy từ CalculationResult.ticketPrices
        for (Seat seat : seats) {
            BigDecimal price = calcResult.getTicketPrices()
                    .getOrDefault(seat.getId(), BigDecimal.ZERO);

            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime)
                    .seat(seat)
                    .price(price)
                    .status(TicketStatus.PROCESSING)
                    .build();

            booking.addTicket(ticket);
        }

        // Tạo BookingExtra
        if (request.getExtras() != null) {
            for (ExtraService ex : extraServices) {
                int qty = request.getExtras().get(ex.getId());
                BookingExtra be = BookingExtra.builder()
                        .extraService(ex)
                        .quantity(qty)
                        .totalPrice(ex.getPrice().multiply(BigDecimal.valueOf(qty)))
                        .build();
                booking.addBookingExtra(be);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Step 5 – getResult: Trả về sản phẩm (Booking entity)
    // ═══════════════════════════════════════════════════════════
    @Override
    public Booking getResult() {
        if (booking == null) {
            throw new IllegalStateException(
                    "Booking chưa được build. Director phải gọi buildEntities() trước.");
        }
        return booking;
    }

    // ─── Private: Đóng gói DTO cho Engine ────────────────────────────────────
    private CalculationRequest buildCalculationRequest() {
        List<SeatInfo> seatInfos = seats.stream()
                .map(s -> new SeatInfo(
                        s.getId(),
                        s.getSeatType() != null
                                ? BigDecimal.valueOf(s.getSeatType().getSurcharge())
                                : BigDecimal.ZERO
                ))
                .toList();

        List<ExtraLineItem> extraItems = new ArrayList<>();
        if (request.getExtras() != null && extraServices != null) {
            for (ExtraService ex : extraServices) {
                Integer qty = request.getExtras().get(ex.getId());
                if (qty != null) {
                    extraItems.add(new ExtraLineItem(ex, qty));
                }
            }
        }

        return new CalculationRequest(
                request.getBookingType(),
                BigDecimal.valueOf(showtime.getBasePrice()),
                seatInfos,
                promotion,
                extraItems
        );
    }
}
