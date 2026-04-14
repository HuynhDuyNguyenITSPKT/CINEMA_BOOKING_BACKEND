package com.movie.cinema_booking_backend.enums;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

public enum BookingStatus {
    PENDING {
        @Override
        public BookingStatus approve() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn đặt vé này không phải vé đoàn, không cần duyệt.");
        }

        @Override
        public BookingStatus initiatePayment() {
            return PENDING;
        }

        @Override
        public BookingStatus pay() {
            return SUCCESS;
        }

        @Override
        public BookingStatus cancel() {
            return CANCELLED;
        }
    },
    PENDING_APPROVAL {
        @Override
        public BookingStatus approve() {
            return RESERVED;
        }

        @Override
        public BookingStatus initiatePayment() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn đặt vé đoàn chưa được duyệt, không thể khởi tạo thanh toán.");
        }

        @Override
        public BookingStatus pay() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn đặt vé đoàn chưa được duyệt, không thể thanh toán.");
        }

        @Override
        public BookingStatus cancel() {
            return CANCELLED;
        }
    },
    RESERVED {
        @Override
        public BookingStatus approve() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn đặt vé này đã được duyệt rồi.");
        }

        @Override
        public BookingStatus initiatePayment() {
            return PENDING;
        }

        @Override
        public BookingStatus pay() {
            return SUCCESS;
        }

        @Override
        public BookingStatus cancel() {
            return CANCELLED;
        }
    },
    SUCCESS {
        @Override
        public BookingStatus approve() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Đơn đặt vé đã thanh toán, không thể duyệt lại.");
        }

        @Override
        public BookingStatus initiatePayment() {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS_UNSUCCESS, "Đơn đặt vé này đã thanh toán thành công rồi.");
        }

        @Override
        public BookingStatus pay() {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS_UNSUCCESS, "Đơn đặt vé này đã thanh toán thành công rồi.");
        }

        @Override
        public BookingStatus cancel() {
            throw new AppException(ErrorCode.BOOKING_ALREADY_PAID);
        }
    },
    CANCELLED {
        @Override
        public BookingStatus approve() {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        @Override
        public BookingStatus initiatePayment() {
             throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        @Override
        public BookingStatus pay() {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        @Override
        public BookingStatus cancel() {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
    },
    REFUNDED {
        @Override
        public BookingStatus approve() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể thao tác trên đơn đã hoàn tiền.");
        }

        @Override
        public BookingStatus initiatePayment() {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể thao tác trên đơn đã hoàn tiền.");
        }

        @Override
        public BookingStatus pay() {
             throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể thao tác trên đơn đã hoàn tiền.");
        }

        @Override
        public BookingStatus cancel() {
             throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể thao tác trên đơn đã hoàn tiền.");
        }
    };

    public abstract BookingStatus approve();
    public abstract BookingStatus initiatePayment();
    public abstract BookingStatus pay();
    public abstract BookingStatus cancel();
}