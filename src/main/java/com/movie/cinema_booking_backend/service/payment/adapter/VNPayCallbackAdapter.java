package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.payment.VNPayService;
import com.movie.cinema_booking_backend.service.payment.service.PaymentCallbackAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VNPayCallbackAdapter implements PaymentCallbackAdapter {

    @Autowired
    private VNPayService vnPayService;

    @Override
    public PaymentResult handleCallback(Map<String, String> data) {

        boolean valid = vnPayService.verify(data);

        if (!valid) {
            throw new RuntimeException("Invalid VNPay signature");
        }

        boolean success = "00".equals(data.get("vnp_ResponseCode"));

        return PaymentResult.builder()
            .orderId(data.get("vnp_TxnRef"))
            .success(success)
            .provider("VNPAY")
            .resultCode(data.getOrDefault("vnp_ResponseCode", ""))
            .message(data.getOrDefault("vnp_OrderInfo", ""))
            .build();
    }
}