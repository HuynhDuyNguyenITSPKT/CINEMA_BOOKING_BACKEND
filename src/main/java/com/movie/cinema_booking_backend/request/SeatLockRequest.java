package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Body cho POST /api/showtimes/{id}/seats/lock */
@Getter
@Setter
public class SeatLockRequest {

    @NotNull
    @NotEmpty(message = "Danh sách seatId không được rỗng")
    private List<String> seatIds;
}
