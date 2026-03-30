package com.movie.cinema_booking_backend.service.bookingticket.proxy;

import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.SeatRepository;
import com.movie.cinema_booking_backend.repository.ShowtimeRepository;
import com.movie.cinema_booking_backend.repository.TicketRepository;
import com.movie.cinema_booking_backend.response.SeatResponse;
import com.movie.cinema_booking_backend.service.ISeatService;
import com.movie.cinema_booking_backend.service.bookingticket.singleton.SeatLockRegistry;
import com.movie.cinema_booking_backend.service.impl.SeatServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: PROXY
 * ═══════════════════════════════════════════════════════════
 *
 * Subject Interface : ISeatService
 * Real Subject      : SeatServiceImpl   (query DB, không biết lock)
 * Proxy (đây)       : SeatValidationProxy (@Primary → Spring inject cái này)
 *
 * Proxy thêm 2 trách nhiệm mà SeatServiceImpl không nên biết:
 *   1. enrichWithLockStatus() — đánh dấu ghế LOCKED/BOOKED/AVAILABLE
 *      dựa trên SeatLockRegistry (RAM) và TicketRepository (DB).
 *   2. validateForBooking()   — 3 checks trước khi tạo booking.
 *
 * Tại sao @Primary?
 *   ISeatService có 2 bean: SeatServiceImpl (không Primary) và Proxy (Primary).
 *   Spring tự chọn @Primary khi inject theo type ISeatService.
 *   → SeatController tự dùng Proxy mà không cần sửa một dòng code nào.
 *
 * Tại sao inject SeatServiceImpl trực tiếp (không qua ISeatService)?
 *   Nếu inject ISeatService → Spring inject Proxy này lại vào chính nó
 *   → Circular dependency. Inject concrete class tránh vòng lặp.
 */
@Primary
@Service
public class SeatValidationProxy implements ISeatService {

    private final SeatServiceImpl realSeatService;
    private final SeatLockRegistry lockRegistry;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    public SeatValidationProxy(SeatServiceImpl realSeatService,
                               SeatLockRegistry lockRegistry,
                               TicketRepository ticketRepository,
                               SeatRepository seatRepository,
                               ShowtimeRepository showtimeRepository) {
        this.realSeatService     = realSeatService;
        this.lockRegistry        = lockRegistry;
        this.ticketRepository    = ticketRepository;
        this.seatRepository      = seatRepository;
        this.showtimeRepository  = showtimeRepository;
    }

    // ─── ISeatService override ──────────────────────────────────────────────

    /** Gọi real service rồi enrich status (dùng cho /auditoriums/{id}/seats). */
    @Override
    public List<SeatResponse> getSeatsByAuditorium(String auditoriumId) {
        List<SeatResponse> seats = realSeatService.getSeatsByAuditorium(auditoriumId);
        // Không có showtimeId → không thể check LOCKED/BOOKED → trả AVAILABLE
        return seats.stream()
                .map(s -> toAvailable(s))
                .collect(Collectors.toList());
    }

    // ─── New: Seat Map by Showtime ──────────────────────────────────────────

    /**
     * Lấy sơ đồ ghế trong ngữ cảnh một suất chiếu cụ thể.
     * Enrich status: BOOKED (DB) > LOCKED (RAM) > AVAILABLE.
     *
     * @param showtimeId ID suất chiếu.
     * @param currentUserId ID user đang xem (để phân biệt ghế LOCKED của mình vs người khác).
     */
    public List<SeatResponse> getSeatMapByShowtime(String showtimeId, String currentUserId) {
        // 1. Validate showtime tồn tại
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        // 2. Query ghế (JOIN FETCH SeatType tránh N+1)
        List<Seat> seats = seatRepository.findByShowtimeIdWithSeatType(showtimeId);

        // 3. Lấy tập hợp seatId đã BOOKED từ DB (O(1) lookup sau đó)
        Set<String> bookedSeatIds = ticketRepository
                .findSeatIdsByShowtimeIdAndStatus(showtimeId, TicketStatus.BOOKED);

        // 4. Enrich từng ghế
        return seats.stream()
                .map(seat -> enrichStatus(seat, showtimeId, bookedSeatIds, currentUserId))
                .collect(Collectors.toList());
    }

    // ─── Validation (dùng bởi BookingFacade Phase 4) ───────────────────────

    /**
     * Validate danh sách ghế trước khi tạo booking. 3 checks:
     *   1. Ghế tồn tại trong auditorium của showtime.
     *   2. Ghế không bị lock bởi user khác (RAM check).
     *   3. Ghế chưa có Ticket BOOKED (DB check — defense in depth).
     *
     * Ném AppException ngay khi phát hiện ghế đầu tiên vi phạm.
     */
    public void validateForBooking(String showtimeId, List<String> seatIds, String userId) {
        // Lấy tập hợp seatId hợp lệ trong showtime này
        Set<String> validSeatIds = seatRepository
                .findByShowtimeIdWithSeatType(showtimeId)
                .stream()
                .map(Seat::getId)
                .collect(Collectors.toSet());

        // Lấy tập hợp seatId đã BOOKED
        Set<String> bookedSeatIds = ticketRepository
                .findSeatIdsByShowtimeIdAndStatus(showtimeId, TicketStatus.BOOKED);

        for (String seatId : seatIds) {
            // Check 1: ghế có thuộc phòng chiếu của showtime này không
            if (!validSeatIds.contains(seatId)) {
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }
            // Check 2: ghế có đang bị lock bởi user khác không
            if (lockRegistry.isLockedByOther(showtimeId, seatId, userId)) {
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
            // Check 3: ghế đã được đặt chưa (DB UniqueConstraint backup)
            if (bookedSeatIds.contains(seatId)) {
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private SeatResponse enrichStatus(Seat seat, String showtimeId,
                                      Set<String> bookedSeatIds, String currentUserId) {
        String status;
        if (bookedSeatIds.contains(seat.getId())) {
            status = "BOOKED";
        } else if (lockRegistry.isLockedByOther(showtimeId, seat.getId(), currentUserId)) {
            status = "LOCKED";
        } else {
            status = "AVAILABLE";
        }

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

    private SeatResponse toAvailable(SeatResponse s) {
        return SeatResponse.builder()
                .id(s.getId()).name(s.getName())
                .rowIndex(s.getRowIndex()).columnIndex(s.getColumnIndex())
                .seatTypeId(s.getSeatTypeId()).seatTypeName(s.getSeatTypeName())
                .seatTypeSurcharge(s.getSeatTypeSurcharge())
                .status("AVAILABLE")
                .build();
    }
}
