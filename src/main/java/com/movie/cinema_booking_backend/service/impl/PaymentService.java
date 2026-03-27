package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.payment.registry.PaymentCallbackRegistry;
import com.movie.cinema_booking_backend.service.payment.registry.PaymentGatewayRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentService implements IPaymentSevice {

    private final PaymentGatewayRegistry gatewayRegistry;
    private final PaymentCallbackRegistry callbackRegistry;

    public PaymentService(PaymentGatewayRegistry gatewayRegistry, PaymentCallbackRegistry callbackRegistry) {
        this.gatewayRegistry = gatewayRegistry;
        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public PaymentResponse pay(String method, PaymentRequest request) {
        return gatewayRegistry.getByMethod(method).createPayment(request);
    }

    @Override
    public PaymentResult handleCallback(String method, Map<String, String> data) {
        return callbackRegistry.getByMethod(method).handleCallback(data);
    }
}
