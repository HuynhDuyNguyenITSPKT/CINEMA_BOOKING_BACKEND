package com.movie.cinema_booking_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auditoriums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 255, unique = true, nullable = false)
    private String name;

    private int seatCount;

    /**
     * Kích thước lưới ghế được lưu để tái dựng layout khi admin ở giao diện sửa phòng chiếu.
     */
    private int totalRows;
    private int totalColumns;

    /**
     * Trạng thái phòng chiếu.
     * Mặc định là ACTIVE khi tạo mới.
     * Dùng bởi AuditoriumState (Phase 4 - State Pattern) để guard tạo Showtime.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuditoriumStatus status = AuditoriumStatus.ACTIVE;

    @OneToMany(mappedBy = "auditorium", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "auditorium")
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Showtime> showtimes = new ArrayList<>();
}
