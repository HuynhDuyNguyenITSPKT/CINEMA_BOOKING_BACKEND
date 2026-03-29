package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.*;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.DiscountType;
import com.movie.cinema_booking_backend.enums.TicketStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: BUILDER
 * ═══════════════════════════════════════════════════════════
 *
 * Vấn đề trước Builder:
 *   VeService.java cũ dùng setter rải rác — mỗi nơi gọi booking.setX() một chỗ.
 *   → Không ai đảm bảo tất cả field đã được set.
 *   → totalAmount phải tính đúng chỉ khi biết đủ seats + extras + promotion.
 *      Nếu tính sớm → sai vì chưa có đủ thông tin.
 *
 * Giải pháp Builder:
 *   Tích lũy từng phần (seats, extras, promotion) → build() tính tổng 1 lần.
 *   build() không thể gọi thiếu user hoặc showtime → fail-fast với IllegalStateException.
 *
 * Không dùng Lombok @Builder trên Booking vì:
 *   - Builder này cần business logic (tính price, tạo Ticket, tạo BookingExtra)
 *   - Lombok @Builder chỉ set field, không chạy được logic.
 *
 * Công thức tính giá 1 Ticket:
 *   ticketPrice = showtime.basePrice + seat.seatType.surcharge
 *
 * Promotion áp dụng trên subtotal (tổng ticket):
 *   PERCENTAGE:   discount = subtotal × (discountValue / 100), cap maxDiscountAmount
 *   FIXED_AMOUNT: discount = min(discountValue, subtotal)
 *
 * totalAmount = subtotal - discount + extrasTotal
 */
public class BookingDraftBuilder {

    private User user;
    private Showtime showtime;
    private final List<Seat> seats     = new ArrayList<>();
    private final List<ExtraLineItem> extras = new ArrayList<>();
    private Promotion promotion;
    private String note;

    // ─── Fluent setters ────────────────────────────────────────────────────

    public BookingDraftBuilder user(User user) {
        this.user = user;
        return this;
    }

    public BookingDraftBuilder showtime(Showtime showtime) {
        this.showtime = showtime;
        return this;
    }

    public BookingDraftBuilder addSeat(Seat seat) {
        this.seats.add(seat);
        return this;
    }

    public BookingDraftBuilder addSeats(List<Seat> seatList) {
        this.seats.addAll(seatList);
        return this;
    }

    public BookingDraftBuilder addExtra(ExtraService extraService, int quantity) {
        this.extras.add(new ExtraLineItem(extraService, quantity));
        return this;
    }

    public BookingDraftBuilder promotion(Promotion promotion) {
        this.promotion = promotion;
        return this;
    }

    public BookingDraftBuilder note(String note) {
        this.note = note;
        return this;
    }

    // ─── Build ─────────────────────────────────────────────────────────────

    /**
     * Tạo Booking + Tickets + BookingExtras.
     * Tính totalAmount tại đây — đảm bảo chính xác vì đủ data.
     *
     * @return Booking chưa được persist (caller gọi bookingRepository.save()).
     */
    public Booking build() {
        // Validation
        if (user == null)     throw new IllegalStateException("BookingDraftBuilder: user is required");
        if (showtime == null) throw new IllegalStateException("BookingDraftBuilder: showtime is required");
        if (seats.isEmpty())  throw new IllegalStateException("BookingDraftBuilder: at least 1 seat required");

        BigDecimal basePrice = BigDecimal.valueOf(showtime.getBasePrice());

        // 1. Tính subtotal từ tickets (basePrice + seatType surcharge)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal surcharge = seat.getSeatType() != null
                    ? BigDecimal.valueOf(seat.getSeatType().getSurcharge())
                    : BigDecimal.ZERO;
            subtotal = subtotal.add(basePrice).add(surcharge);
        }

        // 2. Tính discount từ promotion
        BigDecimal discount = calculateDiscount(subtotal);

        // 3. Tính extras total
        BigDecimal extrasTotal = BigDecimal.ZERO;
        for (ExtraLineItem item : extras) {
            extrasTotal = extrasTotal.add(
                    item.extraService.getPrice().multiply(BigDecimal.valueOf(item.quantity))
            );
        }

        // 4. Final total
        BigDecimal total = subtotal.subtract(discount).add(extrasTotal)
                .max(BigDecimal.ZERO); // không âm

        // 5. Tạo Booking entity
        String bookingId = UUID.randomUUID().toString();
        Booking booking = Booking.builder()
                .id(bookingId)
                .user(user)
                .status(BookingStatus.PENDING)
                .totalAmount(total)
                .createdAt(LocalDateTime.now())
                .note(note)
                .build();

        // 6. Tạo Tickets và attach vào Booking
        for (Seat seat : seats) {
            BigDecimal surcharge = seat.getSeatType() != null
                    ? BigDecimal.valueOf(seat.getSeatType().getSurcharge())
                    : BigDecimal.ZERO;
            BigDecimal ticketPrice = basePrice.add(surcharge)
                    .subtract(discount.divide(BigDecimal.valueOf(seats.size()), 2, RoundingMode.FLOOR));

            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID().toString())
                    .showtime(showtime)
                    .seat(seat)
                    .price(ticketPrice.max(BigDecimal.ZERO))
                    .status(TicketStatus.PROCESSING)
                    .build();

            booking.addTicket(ticket);
        }

        // 7. Tạo BookingExtras và attach vào Booking
        for (ExtraLineItem item : extras) {
            BigDecimal lineTotal = item.extraService.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity));

            BookingExtra bookingExtra = BookingExtra.builder()
                    .extraService(item.extraService)
                    .quantity(item.quantity)
                    .totalPrice(lineTotal)
                    .build();

            booking.addBookingExtra(bookingExtra);
        }

        return booking;
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (promotion == null) return BigDecimal.ZERO;

        BigDecimal discount;
        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.FLOOR);
            // Áp dụng cap maxDiscountAmount nếu có
            if (promotion.getMaxDiscountAmount() != null
                    && promotion.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(promotion.getMaxDiscountAmount());
            }
        } else { // FIXED_AMOUNT
            discount = promotion.getDiscountValue().min(subtotal);
        }

        return discount;
    }

    // ─── Inner helper ───────────────────────────────────────────────────────

    private record ExtraLineItem(ExtraService extraService, int quantity) {}
}
