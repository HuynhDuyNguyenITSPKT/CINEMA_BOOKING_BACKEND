package com.movie.cinema_booking_backend.service.payment.service;

import com.movie.cinema_booking_backend.request.payment.PaymentResult;

import java.util.Map;

public interface PaymentCallbackAdapter {
    PaymentResult handleCallback(Map<String, String> data);
}
