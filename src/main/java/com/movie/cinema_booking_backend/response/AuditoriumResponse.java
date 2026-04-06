package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriumResponse {
    private String id;
    private String name;
    /** Số ghế thực tế đã generate (computed, không nhập tay). */
    private int totalSeats;
    private AuditoriumStatus status;
    /** Kích thước lưới được lưu khi tạo phòng — dùng để tái dựng Grid lúc admin sửa. */
    private int totalRows;
    private int totalColumns;
}

