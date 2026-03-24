package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.payment.adapter.MoMoAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.MoMoCallbackAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.VNPayAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.VNPayCallbackAdapter;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService implements IPaymentSevice{

    @Autowired
    private VNPayAdapter vnPayAdapter;
    
    @Autowired
    private MoMoAdapter moMoAdapter;

    @Autowired
    private VNPayCallbackAdapter vnPayCallbackAdapter;

    @Autowired
    private MoMoCallbackAdapter moMoCallbackAdapter;

    // vi phạm open/closed nhưng để trình bày không sử dụng factory pattern
    @Override
    public PaymentResponse pay(String method, PaymentRequest request) {
        String normalizedMethod = normalizeMethod(method);
        switch (normalizedMethod) {
            case "VNPAY":
                return vnPayAdapter.createPayment(request);
            case "MOMO":
                return moMoAdapter.createPayment(request);
            default:
                throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Override
    public PaymentResult handleCallback(String method, Map<String, String> data) {
        String normalizedMethod = normalizeMethod(method);
        switch (normalizedMethod) {
            case "VNPAY":
                return vnPayCallbackAdapter.handleCallback(data);
            case "MOMO":
                return moMoCallbackAdapter.handleCallback(data);
            default:
                throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String normalizeMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return method.trim().toUpperCase(Locale.ROOT);
    }
}
