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
     * @param seatTypeMap  Map từ tên SeatType (uppercase) → SeatType entity.
     *                     Cần có "STANDARD". "VIP" và "PREMIUM" optional.
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

        // Convert vipRows/premiumRows sang Set<Integer> (1-based) để lookup O(1)
        Set<Integer> vipRowSet    = new HashSet<>(config.getVipRows());
        Set<Integer> premiumRowSet = new HashSet<>(config.getPremiumRows());

        // Convert disabledSeats sang Set<String> (uppercase) để lookup O(1)
        Set<String> disabled = new HashSet<>();
        if (config.getDisabledSeats() != null) {
            config.getDisabledSeats().forEach(s -> disabled.add(s.toUpperCase()));
        }

        SeatType standardType = seatTypeMap.get("STANDARD");
        SeatType vipType      = seatTypeMap.get("VIP");
        SeatType premiumType  = seatTypeMap.get("PREMIUM");

        // Fallback: nếu không có VIP/PREMIUM, dùng STANDARD
        if (vipType == null)     vipType = standardType;
        if (premiumType == null) premiumType = standardType;

        List<Seat> seats = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            char rowChar = (char) ('A' + r);         // 0→'A', 1→'B'
            int  rowNum  = r + 1;                    // 1-based cho lookup vipRows

            for (int c = 0; c < cols; c++) {
                String seatName = String.valueOf(rowChar) + (c + 1); // "A1", "B3"

                // Bỏ qua ghế bị disable
                if (disabled.contains(seatName.toUpperCase())) {
                    continue;
                }

                SeatType seatType;
                if (vipRowSet.contains(rowNum)) {
                    seatType = vipType;
                } else if (premiumRowSet.contains(rowNum)) {
                    seatType = premiumType;
                } else {
                    seatType = standardType;
                }

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
