package com.movie.cinema_booking_backend.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @JsonAlias("token")
    @NotBlank(message = "Google token không được để trống")
    private String tokenId;
}
