package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GenreResponse - DTO trả về cho client.
 *
 * Dùng đầy đủ Lombok annotations để hỗ trợ cả Builder pattern
 * và Jackson deserialization (cần no-args constructor).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponse {
    private String id;
    private String name;
}
