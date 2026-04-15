package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.TicketStatus;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

   @Column(name = "price", nullable = false)
    @Builder.Default
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(length = 500)
    private String qrCodeUrl;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TicketPromotion promotion;

    public void addPromotion(TicketPromotion ticketPromotion) {
        this.promotion = ticketPromotion;
        if (ticketPromotion != null) {
            ticketPromotion.setTicket(this);
        }
    }

    public void removePromotion(TicketPromotion ticketPromotion) {
        if (this.promotion == ticketPromotion) {
            this.promotion.setTicket(null);
            this.promotion = null;
        }
    }

    public void removePromotion() {
        if (this.promotion != null) {
            this.promotion.setTicket(null);
            this.promotion = null;
        }
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void confirmPayment() {
        this.status = this.status.confirmPayment();
    }

    public void checkIn() {
        this.status = this.status.checkIn();
    }

    public void cancel() {
        this.status = this.status.cancel();
    }
}