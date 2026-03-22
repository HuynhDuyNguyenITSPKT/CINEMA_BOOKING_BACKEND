package com.movie.cinema_booking_backend.service.payment.impl;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.SepayService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SepayAdapter implements PaymentGateway {

    @Autowired
    private SepayService sepayService;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        String checkoutPageUrl = sepayService.createCheckoutPageUrl(request);
        return new PaymentResponse(checkoutPageUrl, "SEPAY");
    }
}
