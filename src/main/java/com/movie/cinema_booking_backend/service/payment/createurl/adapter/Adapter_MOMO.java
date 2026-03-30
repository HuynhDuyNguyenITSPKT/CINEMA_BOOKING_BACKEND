package com.movie.cinema_booking_backend.service.payment.createurl.adapter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.payment.MoMoService;

@Service
public class Adapter_MOMO implements IAdapterPay {

    private final MoMoService moMoService;

    public Adapter_MOMO(MoMoService moMoService) {
        this.moMoService = moMoService;
    }

    @Override
    public String createPaymentUrl(PaymentRequest request) {

        Map<String, String> momoParams = new HashMap<>();
        momoParams.put("amount", String.valueOf(request.getAmount()));
        momoParams.put("orderId", request.getBookingId());
        momoParams.put("orderInfo", request.getDescription());

        return moMoService.createPayment(momoParams);
    }
    
}
