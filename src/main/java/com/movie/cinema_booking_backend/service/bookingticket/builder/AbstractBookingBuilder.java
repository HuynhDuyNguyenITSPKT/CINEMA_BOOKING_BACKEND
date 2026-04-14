package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.request.BookingRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.PricingEngine;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.ExtraLineItem;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationRequest.SeatInfo;
import com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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

    protected AbstractBookingBuilder() {
    }

    @Override
    public void reset(BookingRequest request, BookingContext context, String username) {
        this.request       = request;
        this.username      = username;
        this.booking       = null;
        this.calcResult    = null;

        // Điền dữ liệu từ context (Lego pieces)
        this.showtime      = context.showtime();
        this.user          = context.user();
        this.seats         = context.seats();
        this.promotion     = context.promotion();
        this.extraServices = context.extraServices();
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

    /** Dùng cho constructPreview(): Đọc kết quả tính giá không qua buildEntities. */
    public com.movie.cinema_booking_backend.service.bookingticket.engine.dto.CalculationResult getCalcResult() {
        if (calcResult == null) {
            throw new IllegalStateException("Chưa chạy runPricing(). Gọi constructPreview() trước.");
        }
        return calcResult;
    }

    protected CalculationRequest buildCalculationRequest() {
        List<SeatInfo> seatInfos = seats.stream().map(s -> new SeatInfo(
                s.getId(),
                s.getSeatType() != null ? BigDecimal.valueOf(s.getSeatType().getSurchargeAmount()) : BigDecimal.ZERO
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
                BigDecimal.valueOf(showtime.getBaseTicketPrice()),
                seatInfos,
                promotion,
                extraItems
        );
    }
}
