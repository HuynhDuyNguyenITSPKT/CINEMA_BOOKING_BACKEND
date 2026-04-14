package com.movie.cinema_booking_backend.service.payment.createurl.adapter;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.payment.VNPayService;


@Service
public class Adapter_VNPAY implements IAdapterPay {

    private final VNPayService vnPayService;

    public Adapter_VNPAY(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @Override
    // Khai báo type để PaymentService map strategy.
    public String getType() {
        return "vnpay";
    }

    @Override
    // Delegate tạo URL thanh toán cho VNPayService.
    public String createPaymentUrl(PaymentRequest request) {
        return vnPayService.createPaymentUrl(request);
    }
}
