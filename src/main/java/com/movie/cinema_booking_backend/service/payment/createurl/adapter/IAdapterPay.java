package com.movie.cinema_booking_backend.service.payment.createurl.adapter;

import com.movie.cinema_booking_backend.request.PaymentRequest;

public interface IAdapterPay {
    // Trả định danh method thanh toán (ví dụ: vnpay, momo).
    String getType();

    // Tạo link/payUrl theo gateway cụ thể từ request chuẩn hóa chung.
    String createPaymentUrl(PaymentRequest request);
}
