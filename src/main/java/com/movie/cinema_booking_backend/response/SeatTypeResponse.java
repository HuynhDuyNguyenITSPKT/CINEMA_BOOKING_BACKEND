package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTypeResponse {
    private String id;
    private String name;
    private float surchargeAmount;
}
