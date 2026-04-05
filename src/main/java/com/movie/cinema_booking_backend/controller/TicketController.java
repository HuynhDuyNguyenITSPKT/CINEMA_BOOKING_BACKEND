package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.TicketRepository;
import com.movie.cinema_booking_backend.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API quản lý vé.
 * Check-in chỉ dành cho nhân viên rạp chiếu (hoặc ADMIN).
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Nhân viên rạp quét mã QR vé và gọi API này để check-in.
     * Chuyển trạng thái từ BOOKED -> USED nhờ State Pattern (TicketState).
     */
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public ApiResponse<?> checkInTicket(@PathVariable("id") String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

        // State Pattern: uỷ quyền logic kiểm tra hợp lệ và đổi trạng thái
        // Ném ngoại lệ TICKET_ALREADY_USED, TICKET_NOT_PAID, TICKET_CANCELLED nếu vi phạm
        ticket.checkIn();

        ticketRepository.save(ticket);

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Check-in vé thành công!")
                .data(ticket.getStatus().name())
                .build();
    }
}
