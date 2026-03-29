package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.PaymentRequest;

public interface IPayment {
    String createPaymentUrl(String method, PaymentRequest request);  
}
