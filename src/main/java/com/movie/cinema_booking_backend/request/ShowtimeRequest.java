package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeRequest {

    @NotBlank(message = "ID phim không được để trống")
    private String movieId;

    @NotBlank(message = "ID phòng chiếu không được để trống")
    private String auditoriumId;

    @NotNull(message = "Thời gian chiếu không được để trống")
    @Future(message = "Thời gian chiếu phải ở trong tương lai")
    private LocalDateTime startTime;

    @NotNull(message = "Giá vé cơ sở không được để trống")
    @Min(value = 1000, message = "Giá vé cơ sở phải ít nhất 1.000 VND")
    private Integer standardPrice;
}
