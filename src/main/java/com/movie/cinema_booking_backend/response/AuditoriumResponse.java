package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriumResponse {
    private String id;
    private String name;
    private int seatCount;
    private AuditoriumStatus status;
}
