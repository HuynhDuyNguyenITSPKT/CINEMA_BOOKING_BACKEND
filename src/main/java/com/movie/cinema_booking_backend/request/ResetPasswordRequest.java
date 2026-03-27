package com.movie.cinema_booking_backend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NonNull
    private String email;

    @NonNull
    private String otp;

    @NonNull
    private String newPassword;
}
