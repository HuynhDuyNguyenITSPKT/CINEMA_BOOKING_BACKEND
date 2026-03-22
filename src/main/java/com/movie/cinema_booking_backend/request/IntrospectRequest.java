package com.movie.cinema_booking_backend.request;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class IntrospectRequest {
    private String token;
}
