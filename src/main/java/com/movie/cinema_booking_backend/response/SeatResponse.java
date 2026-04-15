package com.movie.cinema_booking_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho từng ghế trong sơ đồ ghế của phòng chiếu.
 *
 * status (Phase 1): chưa có, để null.
 * Phase 2: SeatValidationProxy sẽ enrich thêm AVAILABLE/LOCKED/BOOKED.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private String id;
    private String name;
    private int rowIndex;
    private int columnIndex;
    private String seatTypeId;
    private String seatTypeName;
    private float seatTypeSurcharge;

    /**
     * Trạng thái ghế (AVAILABLE / LOCKED / BOOKED).
     * Phase 1: luôn null. Phase 2 Proxy sẽ enrich giá trị này.
     */
    private String status;

    /**
     * true nếu ghế đang được giữ bởi chính user hiện tại.
     * Frontend dùng cờ này để đồng bộ selectedIds theo lock thực từ backend.
     */
    private Boolean lockedByCurrentUser;
}
