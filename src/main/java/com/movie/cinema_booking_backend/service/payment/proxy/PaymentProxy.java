package com.movie.cinema_booking_backend.service.payment.proxy;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.impl.PaymentService;

@Service
public class PaymentProxy implements IPaymentSevice{

    private PaymentService paymentService;
    
    public PaymentProxy(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    public PaymentResponse pay(String method, PaymentRequest request) {
        // You can add additional logic here (e.g., logging, validation, etc.)
        return paymentService.pay(method, request);
    }
    
    @Override
    public PaymentResult handleCallback(String method, Map<String, String> data) {
        // You can add additional logic here (e.g., logging, validation, etc.)
        return paymentService.handleCallback(method, data);
    }
    
}
