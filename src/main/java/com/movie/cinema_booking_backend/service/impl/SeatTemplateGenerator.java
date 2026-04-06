package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.entity.SeatType;
import com.movie.cinema_booking_backend.enums.SeatStatus;
import com.movie.cinema_booking_backend.request.SeatLayoutConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * SeatTemplateGenerator — Tạo danh sách Seat từ SeatLayoutConfig.
 *
 * Quy tắc đặt tên ghế:
 *   - Hàng (row) → chữ cái: row 0 = 'A', row 1 = 'B', ..., row 25 = 'Z'
 *   - Cột (col)  → số 1-based: col 0 = "1", col 1 = "2"
 *   - Tên ghế = Chữ hàng + Số cột → "A1", "B3", "J20"
 *
 * Quy tắc gán SeatType:
 *   - vipRows    (1-based) → SeatType tên "VIP"
 *   - premiumRows(1-based) → SeatType tên "PREMIUM"
 *   - còn lại             → SeatType tên "STANDARD"
 *   - Nếu không tìm thấy SeatType tương ứng → dùng STANDARD
 *
 * Giới hạn: tối đa 26 hàng (A-Z). Nếu totalRows > 26, ném IllegalArgumentException.
 */
@Service
public class SeatTemplateGenerator {

    private static final int MAX_ROWS = 26;

    /**
     * @param auditorium   Phòng chiếu đã được save (có ID).
     * @param config       Layout config từ request.
     * @param seatTypeMap  Map từ ID SeatType → SeatType entity.
     * @return Danh sách Seat chưa save (caller gọi seatRepository.saveAll).
     */
    public List<Seat> generate(Auditorium auditorium,
                               SeatLayoutConfig config,
                               java.util.Map<String, SeatType> seatTypeMap) {

        int rows = config.getTotalRows();
        int cols = config.getTotalColumns();

        if (rows > MAX_ROWS) {
            throw new IllegalArgumentException(
                    "Số hàng tối đa là " + MAX_ROWS + " (A-Z). Yêu cầu: " + rows);
        }

        // Kiểm tra loại ghế mặc định
        SeatType defaultType = seatTypeMap.get(config.getDefaultSeatTypeId());
        if (defaultType == null) {
            throw new IllegalArgumentException("Không tìm thấy loại ghế mặc định có ID: " + config.getDefaultSeatTypeId());
        }

        // Convert disabledSeats sang Set<String> (uppercase) để lookup O(1)
        Set<String> disabled = new HashSet<>();
        if (config.getDisabledSeats() != null) {
            config.getDisabledSeats().forEach(s -> disabled.add(s.toUpperCase()));
        }

        // Build bản đồ tra cứu nhanh: Tên Ghế -> SeatType
        java.util.Map<String, SeatType> customSeatTypes = new java.util.HashMap<>();
        if (config.getSeatTypeMappings() != null) {
            config.getSeatTypeMappings().forEach((typeId, seatNames) -> {
                SeatType type = seatTypeMap.get(typeId);
                if (type != null && seatNames != null) {
                    seatNames.forEach(name -> customSeatTypes.put(name.toUpperCase(), type));
                }
            });
        }

        List<Seat> seats = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);         // 0→'A', 1→'B'

            for (int c = 0; c < cols; c++) {
                String seatName = String.valueOf(rowChar) + (c + 1); // "A1", "B3"
                String upperName = seatName.toUpperCase();

                // Bỏ qua ghế bị disable
                if (disabled.contains(upperName)) {
                    continue;
                }

                // Gán loại ghế từ map custom, nếu không có thì lấy loại mặc định
                SeatType seatType = customSeatTypes.getOrDefault(upperName, defaultType);

                Seat seat = Seat.builder()
                        .name(seatName)
                        .rowIndex(r)
                        .columnIndex(c)
                        .status(SeatStatus.AVAILABLE)
                        .auditorium(auditorium)
                        .seatType(seatType)
                        .build();

                seats.add(seat);
            }
        }

        return seats;
    }
}
