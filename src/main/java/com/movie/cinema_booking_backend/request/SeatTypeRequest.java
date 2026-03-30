package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatTypeRequest {

    @NotBlank(message = "Tên loại ghế không được để trống")
    private String name;

    /**
     * Phụ phí áp dụng thêm so với giá vé cơ bản.
     * VD: STANDARD = 0, VIP = 50_000, COUPLE = 80_000
     */
    @Min(value = 0, message = "Phụ phí không được âm")
    private float surcharge;
}
