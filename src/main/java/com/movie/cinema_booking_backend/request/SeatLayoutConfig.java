package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cấu hình layout phòng chiếu dùng khi tạo hoặc re-generate ghế.
 *
 * Ví dụ phòng 10×20 = 200 ghế, ghế A1 và J20 không dùng, set loại ghế cho các ô cụ thể:
 * {
 *   "totalRows": 10,
 *   "totalColumns": 20,
 *   "seatTypeMappings": {
 *       "e9b4...": ["A3", "A4"],
 *       "f3a2...": ["B1", "B2"]
 *   },
 *   "defaultSeatTypeId": "a1b2...",
 *   "disabledSeats": ["A1", "J20"]
 * }
 *
 * Tên ghế sinh ra theo format: (char)('A' + rowIndex) + (colIndex + 1)
 *   → row 0, col 0  = "A1"
 *   → row 9, col 19 = "J20"
 */
@Getter
@Setter
public class SeatLayoutConfig {

    @Min(value = 1, message = "Số hàng phải ít nhất là 1")
    @NotNull(message = "Số hàng không được để trống")
    private Integer totalRows;

    @Min(value = 1, message = "Số cột phải ít nhất là 1")
    @NotNull(message = "Số cột không được để trống")
    private Integer totalColumns;

    /**
     * Ánh xạ danh sách ghế theo UUID của SeatType.
     * VD: {"seat-type-id-1": ["A3", "A4"], "seat-type-id-vip": ["D5", "D6"]}
     */
    private Map<String, List<String>> seatTypeMappings = new HashMap<>();

    /**
     * ID loại ghế mặc định cho các ghế không thuộc seatTypeMappings.
     * Bắt buộc có để tạo ghế còn lại thành công.
     */
    @NotBlank(message = "ID loại ghế mặc định (defaultSeatTypeId) không được để trống")
    private String defaultSeatTypeId;

    /**
     * Danh sách tên ghế bị vô hiệu hoá (không tạo Seat record).
     * VD: ["A1", "A20"] — ghế ở góc không dùng.
     */
    private List<String> disabledSeats = new ArrayList<>();
}
