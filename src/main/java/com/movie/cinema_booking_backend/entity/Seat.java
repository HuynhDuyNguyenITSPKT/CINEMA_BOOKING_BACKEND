package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "auditorium_id")
    @JsonIgnore
    private Auditorium auditorium;

    @ManyToOne
    @JoinColumn(name = "seat_type_id")
    private SeatType seatType;
}