package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditoriumRequest {

    @NotBlank(message = "Tên phòng chiếu không được để trống")
    private String name;

    @Min(value = 1, message = "Số ghế phải ít nhất là 1")
    private int seatCount;

    /**
     * Trạng thái phòng chiếu. Mặc định ACTIVE nếu không truyền lên.
     */
    @NotNull(message = "Trạng thái phòng chiếu không được để trống")
    private AuditoriumStatus status = AuditoriumStatus.ACTIVE;
}
