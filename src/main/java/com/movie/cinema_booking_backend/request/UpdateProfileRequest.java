package com.movie.cinema_booking_backend.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @NonNull
    private String fullName;

    @NonNull
    private String email;

    @NonNull
    private String phone;

    @NonNull
    private LocalDate dateOfBirth;
}
