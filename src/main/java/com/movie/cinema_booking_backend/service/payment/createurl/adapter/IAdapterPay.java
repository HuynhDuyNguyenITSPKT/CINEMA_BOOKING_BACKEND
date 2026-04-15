package com.movie.cinema_booking_backend.service.payment.createurl.adapter;

import com.movie.cinema_booking_backend.request.PaymentRequest;

public interface IAdapterPay {
    String getType();

    String createPaymentUrl(PaymentRequest request);
}
