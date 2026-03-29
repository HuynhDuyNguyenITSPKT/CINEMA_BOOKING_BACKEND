package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình layout phòng chiếu dùng khi tạo hoặc re-generate ghế.
 *
 * Ví dụ phòng 10×20 = 200 ghế, hàng 9-10 là VIP, ghế A1 và J20 không dùng:
 * {
 *   "totalRows": 10,
 *   "totalColumns": 20,
 *   "vipRows": [9, 10],
 *   "premiumRows": [7, 8],
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
     * Chỉ số hàng (1-based) được gán là VIP.
     * VD: [9, 10] = hàng I và J tính từ đầu phòng.
     */
    private List<Integer> vipRows = new ArrayList<>();

    /**
     * Chỉ số hàng (1-based) được gán là Premium (giữa Standard và VIP).
     */
    private List<Integer> premiumRows = new ArrayList<>();

    /**
     * Danh sách tên ghế bị vô hiệu hoá (không tạo Seat record).
     * VD: ["A1", "A20"] — ghế ở góc không dùng.
     */
    private List<String> disabledSeats = new ArrayList<>();
}
