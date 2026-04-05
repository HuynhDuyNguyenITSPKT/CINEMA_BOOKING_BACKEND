package com.movie.cinema_booking_backend.service.bookingticket.state;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

/**
 * Trạng thái vé đã bị huỷ.
 */
public class CancelledState implements TicketState {

    @Override
    public void checkIn(Ticket ticket) throws AppException {
        // Không thể quét vé đã huỷ
        throw new AppException(ErrorCode.TICKET_CANCELLED);
    }

    @Override
    public void cancel(Ticket ticket) throws AppException {
        // Tránh huỷ nhiều lần trên cùng 1 vé
        throw new AppException(ErrorCode.TICKET_CANCELLED);
    }

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.CANCELLED;
    }
}
