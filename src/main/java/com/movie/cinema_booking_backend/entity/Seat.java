package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "NVARCHAR(20)", nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer rowIndex;

    @Column(nullable = false)
    private Integer columnIndex;

    /**
     * Trạng thái ghế được persist vào DB.
     * AVAILABLE: ghế trống.
     * BOOKED: đã có Ticket BOOKED trong DB.
     * LOCKED không lưu DB — chỉ tồn tại trong SeatLockRegistry (Phase 2).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "auditorium_id")
    @JsonIgnore
    private Auditorium auditorium;

    @ManyToOne
    @JoinColumn(name = "seat_type_id")
    private SeatType seatType;
}