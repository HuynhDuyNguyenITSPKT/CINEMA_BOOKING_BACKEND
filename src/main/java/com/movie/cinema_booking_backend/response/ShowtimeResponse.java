package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {
    private String id;
    private int basePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String movieId;
    private String movieTitle;
    private String auditoriumId;
    private String auditoriumName;
}
