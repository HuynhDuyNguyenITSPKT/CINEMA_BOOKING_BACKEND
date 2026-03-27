package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.service.payment.MoMoService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MoMoAdapter implements PaymentGateway {

    private final MoMoService moMoService;

    public MoMoAdapter(MoMoService moMoService) {
        this.moMoService = moMoService;
    }

    @Override
    public String method() {
        return "MOMO";
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {

        Map<String, String> params = new HashMap<>();
        params.put("amount", String.valueOf(request.getAmount()));
        params.put("orderId", request.getBookingId());
        params.put("orderInfo", request.getDescription());


        String payUrl = moMoService.createPayment(params);

        // return new PaymentResponse(payUrl, "MOMO");
        return PaymentResponse.builder()
                .payUrl(payUrl)
                .provider("MOMO")
                .build();
    }
}