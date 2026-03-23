package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.MoMoService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentCallbackAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MoMoCallbackAdapter implements PaymentCallbackAdapter {

    @Autowired
    private MoMoService moMoService;

    @Override
    public PaymentResult handleCallback(Map<String, String> data) {

        boolean valid = moMoService.verify(data);

        if (!valid) {
            throw new RuntimeException("Invalid MoMo signature");
        }

        boolean success = "0".equals(data.get("resultCode"));

        return new PaymentResult(
                data.get("orderId"),
                success
        );
    }
}
