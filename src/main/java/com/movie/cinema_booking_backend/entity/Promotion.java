package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal maxDiscountAmount = BigDecimal.ZERO;

    @Column(name = "min_ticket_required")
    @Builder.Default
    private Integer minTicketRequired = 1;

    @Column(name = "min_order_value")
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    // private String requiredSeatType; (Chỉ áp dụng ghế COUPLE)
    // private String requiredPaymentMethod; (Chỉ áp dụng khi thanh toán MOMO)

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean isActive;

    private String imageUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TicketPromotion> tickets = new ArrayList<>();

    public boolean isEligible(Booking booking) {
        if (this.minTicketRequired != null && booking.getTickets().size() < this.minTicketRequired) {
            return false;
        }
        if (this.minOrderValue != null && this.minOrderValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rawTotal = booking.getTickets().stream()
                    .map(Ticket::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (rawTotal.compareTo(this.minOrderValue) < 0) {
                return false;
            }
        }
        return true;
    }

    public Promotion orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}