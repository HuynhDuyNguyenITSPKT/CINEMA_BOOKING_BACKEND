package com.movie.cinema_booking_backend.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NonNull
    private String oldPassword;

    @NonNull
    private String newPassword;
}
