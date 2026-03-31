package com.movie.cinema_booking_backend.service.bookingticket.state;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;

/**
 * Trạng thái vé đã thanh toán thành công, sẵn sàng sử dụng.
 */
public class BookedState implements TicketState {

    @Override
    public void checkIn(Ticket ticket) throws AppException {
        // Đổi trạng thái sang USED thành công
        ticket.setStatus(TicketStatus.USED);
    }

    @Override
    public void cancel(Ticket ticket) throws AppException {
        // Vé đã thanh toán có thể huỷ (cần quy trình refund tuỳ policy)
        ticket.setStatus(TicketStatus.CANCELLED);
    }

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.BOOKED;
    }
}
