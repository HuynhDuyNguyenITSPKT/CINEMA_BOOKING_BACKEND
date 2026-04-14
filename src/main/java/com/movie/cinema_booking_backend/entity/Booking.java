package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private LocalDateTime createdAt;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal grandTotalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment;

    @JsonIgnore
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ticket> tickets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BookingExtra> bookingExtras = new ArrayList<>();

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setBooking(this);
    }

    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
        ticket.setBooking(null);
    }

    public void addBookingExtra(BookingExtra extra) {
        this.bookingExtras.add(extra);
        extra.setBooking(this);
    }

    public void removeBookingExtra(BookingExtra extra) {
        this.bookingExtras.remove(extra);
        extra.setBooking(null);
    }
    public void assignPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setBooking(this);
        }
    }

    // --- State Transition Helpers ---
    public void approve() {
        this.status = this.status.approve();
    }

    public void initiatePayment() {
        this.status = this.status.initiatePayment();
    }

    public void pay() {
        this.status = this.status.pay();
    }

    public void cancel() {
        this.status = this.status.cancel();
    }
}