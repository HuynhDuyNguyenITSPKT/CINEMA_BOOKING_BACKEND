package com.movie.cinema_booking_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Double discountValue;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean isActive;

    private String imageUrl;

    @OneToMany(mappedBy = "promotion")
    @Builder.Default
    private List<TicketPromotion> tickets = new ArrayList<>();
}