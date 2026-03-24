package com.movie.cinema_booking_backend.service;

import java.util.Map;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;

public interface IPaymentSevice {

    public PaymentResponse pay(String method, PaymentRequest request);

    public PaymentResult handleCallback(String method, Map<String, String> data);
    
}
