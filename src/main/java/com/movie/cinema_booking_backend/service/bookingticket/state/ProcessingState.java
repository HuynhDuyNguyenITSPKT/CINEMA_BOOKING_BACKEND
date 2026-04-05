package com.movie.cinema_booking_backend.service.bookingticket.state;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

/**
 * Trạng thái vé đang đợi thanh toán.
 */
public class ProcessingState implements TicketState {

    @Override
    public void checkIn(Ticket ticket) throws AppException {
        // Chưa thanh toán thì không được check-in
        throw new AppException(ErrorCode.TICKET_NOT_PAID);
    }

    @Override
    public void cancel(Ticket ticket) throws AppException {
        // Đang chờ thanh toán -> Có thể huỷ bình thường
        ticket.setStatus(TicketStatus.CANCELLED);
    }

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.PROCESSING;
    }
}
