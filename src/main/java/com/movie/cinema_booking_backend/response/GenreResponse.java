package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GenreResponse — DTO trả về client cho Genre (thể loại phim).
 *
 * OOP — Immutable: chỉ @Getter, không có setter.
 * Builder: khởi tạo qua Lombok @Builder trong GenreFactory.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponse {
    private String id;
    private String name;
}
