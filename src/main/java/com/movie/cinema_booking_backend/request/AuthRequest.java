package com.movie.cinema_booking_backend.request;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
}
