package com.movie.cinema_booking_backend.response;

import java.time.LocalDate;

import com.movie.cinema_booking_backend.enums.Role;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private LocalDate dateOfBirth;
}
