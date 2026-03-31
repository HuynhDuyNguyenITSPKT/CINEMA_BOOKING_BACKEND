package com.movie.cinema_booking_backend.service.bookingticket.state;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: STATE
 * ════════════════════════════════════════════════════════════
 *
 * Interface định nghĩa các hành vi phụ thuộc vào trạng thái của Ticket.
 * Thay vì dùng switch-case (if status == BOOKED then ... else if ...),
 * ta đưa logic check-in và huỷ vé vào các lớp cụ thể implements interface này.
 */
public interface TicketState {

    /**
     * Chuyển trạng thái khi user đến rạp quét mã QR check-in.
     */
    void checkIn(Ticket ticket) throws AppException;

    /**
     * Chuyển trạng thái khi user hoặc hệ thống báo huỷ vé.
     */
    void cancel(Ticket ticket) throws AppException;

    /**
     * Trả về Enum TicketStatus tương ứng với State hiện tại để lưu Database.
     */
    TicketStatus getStatus();
}
