package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        // Trong 1 suất chiếu (showtime_id), 1 cái ghế (seat_id) chỉ được phép tồn tại 1 lần duy nhất!
        @UniqueConstraint(columnNames = {"showtime_id", "seat_id"})
})
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

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(length = 500)
    private String qrCodeUrl;

    @OneToMany(mappedBy = "ticket")
    @JsonIgnore
    @Builder.Default
    private List<TicketPromotion> promotions = new ArrayList<>();

    public void addPromotion(TicketPromotion ticketPromotion) {
        this.promotions.add(ticketPromotion);
        ticketPromotion.setTicket(this);
    }

    public void removePromotion(TicketPromotion ticketPromotion) {
        this.promotions.remove(ticketPromotion);
        ticketPromotion.setTicket(null);
    }
}