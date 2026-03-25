package com.movie.cinema_booking_backend.request;

import java.time.LocalDate;
import lombok.*;
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class RegistrationRequest {
    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String email;

    @NonNull
    private String fullName;

    @NonNull
    private String phone;

    @NonNull
    private LocalDate dateOfBirth;
}
