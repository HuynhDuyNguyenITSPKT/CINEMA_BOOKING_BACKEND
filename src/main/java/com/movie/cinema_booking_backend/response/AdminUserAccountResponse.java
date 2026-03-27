package com.movie.cinema_booking_backend.response;

import java.time.LocalDate;

import com.movie.cinema_booking_backend.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserAccountResponse {
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Role role;
    private boolean active;
}
