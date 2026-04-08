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
     * - create/regenerate-seats: bắt buộc phải có (được validate ở service).
     * - update metadata: có thể null khi chỉ đổi tên/trạng thái.
     */
    @Valid
    private SeatLayoutConfig seatLayout;
}

