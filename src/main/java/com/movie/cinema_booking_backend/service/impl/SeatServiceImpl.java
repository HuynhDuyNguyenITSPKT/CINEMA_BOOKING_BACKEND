package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.SeatRepository;
import com.movie.cinema_booking_backend.response.SeatResponse;
import com.movie.cinema_booking_backend.service.ISeatService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SeatServiceImpl — "Real Subject" trong Proxy Pattern.
 *
 * Phase 1: chỉ đơn giản query DB + map sang SeatResponse. status = null.
 * Phase 2: SeatValidationProxy sẽ wrap class này (@Primary @Service),
 *           gọi getSeatsByAuditorium() rồi enrich thêm trạng thái AVAILABLE/LOCKED/BOOKED.
 *
 * Lý do đặt @Service (không @Primary): để Phase 2 Proxy có thể inject trực tiếp
 * bằng type cụ thể (SeatServiceImpl), tránh circular dependency.
 */
@Service
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;

    public SeatServiceImpl(SeatRepository seatRepository,
                           AuditoriumRepository auditoriumRepository) {
        this.seatRepository = seatRepository;
        this.auditoriumRepository = auditoriumRepository;
    }

    @Override
    public List<SeatResponse> getSeatsByAuditorium(String auditoriumId) {
        // Validate auditorium tồn tại
        if (!auditoriumRepository.existsById(auditoriumId)) {
            throw new AppException(ErrorCode.AUDITORIUM_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findByAuditoriumIdWithSeatType(auditoriumId);

        return seats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .name(seat.getName())
                .rowIndex(seat.getRowIndex())
                .columnIndex(seat.getColumnIndex())
                .seatTypeId(seat.getSeatType() != null ? seat.getSeatType().getId() : null)
                .seatTypeName(seat.getSeatType() != null ? seat.getSeatType().getName() : null)
                .seatTypeSurcharge(seat.getSeatType() != null ? seat.getSeatType().getSurcharge() : 0f)
                .status(null) // Phase 2 Proxy sẽ enrich
                .build();
    }
}
