package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.SeatRepository;
import com.movie.cinema_booking_backend.repository.TicketRepository;
import com.movie.cinema_booking_backend.response.SeatResponse;
import com.movie.cinema_booking_backend.service.ISeatService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
@RequiredArgsConstructor
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final TicketRepository ticketRepository;
    private static final Set<TicketStatus> OCCUPIED_STATUSES = EnumSet.of(TicketStatus.PROCESSING, TicketStatus.BOOKED, TicketStatus.USED);

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByAuditorium(String auditoriumId){
        if (!auditoriumRepository.existsById(auditoriumId)){
            throw new AppException(ErrorCode.AUDITORIUM_NOT_FOUND);
        }
        List<Seat> seats = seatRepository.findByAuditoriumIdWithSeatType(auditoriumId);
        return seats.stream().map(this::toBasicResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatMapByShowtime(String showtimeId, String currentUserId){
        List<Seat> seats = seatRepository.findByShowtimeIdWithSeatType(showtimeId);
        Set<String> bookedSeatIds = ticketRepository.findSeatIdsByShowtimeIdAndStatuses(showtimeId, OCCUPIED_STATUSES);
        return seats.stream().map(seat -> {
            String status = bookedSeatIds.contains(seat.getId()) ? "BOOKED" : "AVAILABLE";
            return toResponseWithStatus(seat, status);
        }).collect(Collectors.toList());
    }

    private SeatResponse toBasicResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .name(seat.getName())
                .rowIndex(seat.getRowIndex())
                .columnIndex(seat.getColumnIndex())
                .seatTypeId(seat.getSeatType() != null ? seat.getSeatType().getId() : null)
                .seatTypeName(seat.getSeatType() != null ? seat.getSeatType().getName() : null)
                .seatTypeSurcharge(seat.getSeatType() != null ? seat.getSeatType().getSurcharge() : 0f)
                .status(null)
                .build();
    }
    private SeatResponse toResponseWithStatus(Seat seat, String status) {
        return SeatResponse.builder()
                .id(seat.getId())
                .name(seat.getName())
                .rowIndex(seat.getRowIndex())
                .columnIndex(seat.getColumnIndex())
                .seatTypeId(seat.getSeatType() != null ? seat.getSeatType().getId() : null)
                .seatTypeName(seat.getSeatType() != null ? seat.getSeatType().getName() : null)
                .seatTypeSurcharge(seat.getSeatType() != null ? seat.getSeatType().getSurcharge() : 0f)
                .status(status)
                .build();
    }
}
