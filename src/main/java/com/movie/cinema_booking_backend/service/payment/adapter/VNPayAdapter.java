package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.VNPayService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VNPayAdapter implements PaymentGateway {

    @Autowired
    private VNPayService vnPayService;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        String url = vnPayService.createPaymentUrl(request);

        return new PaymentResponse(url, "VNPAY");
    }
}