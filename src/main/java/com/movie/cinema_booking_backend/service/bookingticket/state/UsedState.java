package com.movie.cinema_booking_backend.service.bookingticket.state;

import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

/**
 * Trạng thái vé đã sử dụng (đã quét QR vào rạp).
 */
public class UsedState implements TicketState {

    @Override
    public void checkIn(Ticket ticket) throws AppException {
        // Vé đã dùng rồi không check-in lại được
        throw new AppException(ErrorCode.TICKET_ALREADY_USED);
    }

    @Override
    public void cancel(Ticket ticket) throws AppException {
        // Đã xem phim xong thì không được huỷ hoàn tiền
        throw new AppException(ErrorCode.TICKET_ALREADY_USED);
    }

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.USED;
    }
}
