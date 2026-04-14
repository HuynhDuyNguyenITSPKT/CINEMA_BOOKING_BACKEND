package com.movie.cinema_booking_backend.enums;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

public enum TicketStatus {
    PROCESSING {
        @Override
        public TicketStatus checkIn() {
            throw new AppException(ErrorCode.TICKET_NOT_PAID);
        }

        @Override
        public TicketStatus confirmPayment() {
            return BOOKED;
        }

        @Override
        public TicketStatus cancel() {
            return CANCELLED;
        }
    },
    BOOKED {
        @Override
        public TicketStatus checkIn() {
            return USED;
        }

        @Override
        public TicketStatus confirmPayment() {
             throw new AppException(ErrorCode.INVALID_REQUEST, "Vé này đã được xác nhận thanh toán rồi.");
        }

        @Override
        public TicketStatus cancel() {
            return CANCELLED;
        }
    },
    CANCELLED {
        @Override
        public TicketStatus checkIn() {
            throw new AppException(ErrorCode.TICKET_CANCELLED);
        }

        @Override
        public TicketStatus confirmPayment() {
            throw new AppException(ErrorCode.TICKET_CANCELLED);
        }

        @Override
        public TicketStatus cancel() {
            throw new AppException(ErrorCode.TICKET_CANCELLED);
        }
    },
    USED {
        @Override
        public TicketStatus checkIn() {
            throw new AppException(ErrorCode.TICKET_ALREADY_USED);
        }

        @Override
        public TicketStatus confirmPayment() {
            throw new AppException(ErrorCode.TICKET_ALREADY_USED);
        }

        @Override
        public TicketStatus cancel() {
            throw new AppException(ErrorCode.TICKET_ALREADY_USED);
        }
    };

    public abstract TicketStatus checkIn();
    public abstract TicketStatus confirmPayment();
    public abstract TicketStatus cancel();
}
