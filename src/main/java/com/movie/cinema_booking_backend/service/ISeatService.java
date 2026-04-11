package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.response.SeatResponse;

import java.util.List;

/**
 * Interface cho Seat service.
 *
 * Phase 1: getSeatsByAuditorium() — query thô từ DB, status = null.
 * Phase 2: interface này được dùng làm Subject trong Proxy Pattern.
 *          SeatValidationProxy sẽ implement @Primary, gọi real rồi
 *          enrich thêm status = AVAILABLE/LOCKED/BOOKED.
 */
public interface ISeatService {
    /**
     * UC-BK-04: Lấy danh sách ghế theo phòng (cơ bản)
     * @param auditoriumId ID phòng chiếu
     * @return Danh sách ghế với thông tin cơ bản (status = null hoặc AVAILABLE)
     */
    List<SeatResponse> getSeatsByAuditorium(String auditoriumId);

    /**
     * UC-BK-05: Lấy seat-map theo suất chiếu với trạng thái live
     * @param showtimeId ID suất chiếu
     * @param currentUserId ID user đang xem (để phân biệt lock của mình vs người khác)
     * @return Danh sách ghế với status: AVAILABLE / LOCKED / BOOKED
     */
    List<SeatResponse> getSeatMapByShowtime(String showtimeId, String currentUserId);
}

