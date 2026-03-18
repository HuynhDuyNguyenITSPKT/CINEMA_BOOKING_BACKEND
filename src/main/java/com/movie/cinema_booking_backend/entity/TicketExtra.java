package com.movie.cinema_booking_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_service_id", nullable = false)
    private ExtraService extraService;
}