package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.ExtraLineItem;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.SeatInfo;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: BUILDER (GoF - Abstract Builder)
 * ═══════════════════════════════════════════════════════════
 *
 * Chứa code dùng chung cho mọi luồng Booking (Database loading,
 * giao tiếp với Pricing Engine).
 * Các Concrete class (Standard, Couple, Group) sẽ kế thừa
 * class này và cung cấp riêng logic validateRules() / buildEntities().
 *
 * (Lưu ý: Không đặt @Component ở đây)
 */
public abstract class AbstractBookingBuilder implements BookingBuilder {

    protected final BookingRepository    bookingRepo;
    protected final SeatRepository       seatRepo;
    protected final ShowtimeRepository   showtimeRepo;
    protected final ExtraServiceRepository extraRepo;
    protected final PromotionRepository  promoRepo;
    protected final AccountRepository    accountRepo;

    // ─── State ───────────────────────────────────────────
    protected BookingRequest  request;
    protected String          username;

    protected User            user;
    protected Showtime        showtime;
    protected List<Seat>      seats;
    protected Promotion       promotion;
    protected List<ExtraService> extraServices;

    protected CalculationResult calcResult;
    protected Booking           booking;

    protected AbstractBookingBuilder(BookingRepository bookingRepo, SeatRepository seatRepo,
                                     ShowtimeRepository showtimeRepo, ExtraServiceRepository extraRepo,
                                     PromotionRepository promoRepo, AccountRepository accountRepo) {
        this.bookingRepo = bookingRepo;
        this.seatRepo = seatRepo;
        this.showtimeRepo = showtimeRepo;
        this.extraRepo = extraRepo;
        this.promoRepo = promoRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    public void reset(BookingRequest request, String username) {
        this.request      = request;
        this.username     = username;
        this.booking      = null;
        this.calcResult   = null;
    }

    @Override
    public void loadEntities() {
        this.showtime = showtimeRepo.findById(request.getShowtimeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        this.user = accountRepo.findByUsername(username)
                .map(Account::getUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        this.seats = new ArrayList<>();
        for (String seatId : request.getSeatIds()) {
            Seat seat = seatRepo.findById(seatId)
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
            if (!seat.getAuditorium().getId().equals(showtime.getAuditorium().getId())) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            seats.add(seat);
        }

        String code = request.getPromotionCode();
        this.promotion = (code != null && !code.isBlank())
                ? promoRepo.getPromotionByCode(code).orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND))
                : null;

        this.extraServices = new ArrayList<>();
        if (request.getExtras() != null) {
            for (Long extraId : request.getExtras().keySet()) {
                extraServices.add(extraRepo.findById(extraId)
                        .orElseThrow(() -> new AppException(ErrorCode.EXTRA_SERVICE_NOT_FOUND)));
            }
        }
    }

    // Các class con PHẢI overwrite hàm này
    @Override
    public abstract void validateRules();

    @Override
    public void runPricing(PricingEngine engine) {
        this.calcResult = engine.calculate(buildCalculationRequest());
    }

    // Giao cho con tự build Booking với status tương ứng (RESERVED hoặc PENDING_APPROVAL)
    @Override
    public abstract void buildEntities();

    @Override
    public Booking getResult() {
        if (booking == null) {
            throw new IllegalStateException("Booking chưa được build.");
        }
        return booking;
    }

    protected CalculationRequest buildCalculationRequest() {
        List<SeatInfo> seatInfos = seats.stream().map(s -> new SeatInfo(
                s.getId(),
                s.getSeatType() != null ? BigDecimal.valueOf(s.getSeatType().getSurcharge()) : BigDecimal.ZERO
        )).toList();

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
