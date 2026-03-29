package com.movie.cinema_booking_backend.service.impl;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.payment.createurl.adapter.Adapter_MOMO;
import com.movie.cinema_booking_backend.service.payment.createurl.adapter.Adapter_VNPAY;

@Service
public class PaymentService implements IPayment {

    private final Adapter_VNPAY adapter_vnpay;
    private final Adapter_MOMO adapter_momo;


    public PaymentService(Adapter_VNPAY adapter_vnpay, Adapter_MOMO adapter_momo) {
        this.adapter_vnpay = adapter_vnpay;
        this.adapter_momo = adapter_momo;
    }

    @Override
    public String createPaymentUrl(String method, PaymentRequest request) {
        switch (method.toLowerCase()) {
            case "vnpay":
                return adapter_vnpay.createPaymentUrl(request);
            case "momo":
                return adapter_momo.createPaymentUrl(request);
            default:
                return "";
        }
    }    
}
