package com.movie.cinema_booking_backend.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreResponse {
    private String id;
    private String name;
}
