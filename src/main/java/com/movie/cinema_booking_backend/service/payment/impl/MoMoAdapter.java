package com.movie.cinema_booking_backend.service.payment.impl;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.MoMoService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MoMoAdapter implements PaymentGateway {

    @Autowired
    private MoMoService moMoService;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {

        Map<String, String> params = new HashMap<>();
        params.put("amount", String.valueOf(request.getAmount()));
        params.put("orderId", request.getBookingId());
        params.put("orderInfo", request.getDescription());


        String payUrl = moMoService.createPayment(params);

        return new PaymentResponse(payUrl, "MOMO");
    }
}