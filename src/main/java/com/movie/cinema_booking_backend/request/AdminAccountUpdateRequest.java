package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAccountUpdateRequest {
    private Role role;
    private Boolean active;
}
