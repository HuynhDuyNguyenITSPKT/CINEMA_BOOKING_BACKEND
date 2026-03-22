package com.movie.cinema_booking_backend.service.payment.service;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;

public interface PaymentGateway {
    PaymentResponse createPayment(PaymentRequest request);
}