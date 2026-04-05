package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2CodeExchangeRequest {

    @NotBlank(message = "Code khong duoc de trong")
    private String code;
}
