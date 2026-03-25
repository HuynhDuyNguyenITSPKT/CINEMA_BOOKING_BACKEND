package com.movie.cinema_booking_backend.request;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class OtpRequest {
    private String email;
    private String otp;
}
