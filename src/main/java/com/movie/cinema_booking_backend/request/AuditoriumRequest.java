package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditoriumRequest {

    @NotBlank(message = "Tên phòng chiếu không được để trống")
    private String name;

    @NotNull(message = "Trạng thái phòng chiếu không được để trống")
    private AuditoriumStatus status = AuditoriumStatus.ACTIVE;

    /**
     * Cấu hình layout ghế.
     * Khi tạo phòng mới: bắt buộc phải có để auto-generate ghế.
     * Khi gọi regenerate-seats: dùng để tái cấu hình lại layout.
     * @Valid đảm bảo validate lồng (totalRows, totalColumns min=1).
     */
    @Valid
    @NotNull(message = "Cấu hình layout ghế không được để trống")
    private SeatLayoutConfig seatLayout;
}

