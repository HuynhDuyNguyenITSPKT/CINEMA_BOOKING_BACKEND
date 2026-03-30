package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * GenreRequest \u2014 DTO nh\u1eadn d\u1eef li\u1ec7u t\u1ea1o/c\u1eadp nh\u1eadt th\u1ec3 lo\u1ea1i phim t\u1eeb client.
 *
 * <p>Validation theo chu\u1ea9n Bean Validation (JSR-380).
 * Service kh\u00f4ng c\u1ea7n validate th\u1ee7 c\u00f4ng v\u00ec @Valid \u0111\u00e3 \u0111\u01b0\u1ee3c b\u1eaft bu\u1ed9c t\u1ea1i Controller.
 */
@Data
public class GenreRequest {

    @NotBlank(message = "T\u00ean th\u1ec3 lo\u1ea1i kh\u00f4ng \u0111\u01b0\u1ee3c \u0111\u1ec3 tr\u1ed1ng")
    @Size(max = 100, message = "T\u00ean th\u1ec3 lo\u1ea1i kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 100 k\u00fd t\u1ef1")
    private String name;
}
