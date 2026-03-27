package com.movie.cinema_booking_backend.service.payment.adapter;

import org.springframework.stereotype.Component;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.payment.VNPayService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;

@Component
public class VNPayAdapter implements PaymentGateway {

    private final VNPayService vnPayService;

    public VNPayAdapter(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @Override
    public String method() {
        return "VNPAY";
    }

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