package com.movie.cinema_booking_backend.service.payment.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.payment.VNPayService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;

@Component
public class VNPayAdapter implements PaymentGateway {

    @Autowired
    private VNPayService vnPayService = new VNPayService();

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        String url = vnPayService.createPaymentUrl(request);

        // return new PaymentResponse(url, "VNPAY");

        return PaymentResponse.builder()
                .payUrl(url)
                .provider("VNPAY")
                .build();
    }
}