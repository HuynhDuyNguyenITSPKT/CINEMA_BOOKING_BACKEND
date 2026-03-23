package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.payment.adapter.MoMoAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.MoMoCallbackAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.SepayAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.VNPayAdapter;
import com.movie.cinema_booking_backend.service.payment.adapter.VNPayCallbackAdapter;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private VNPayAdapter vnPayAdapter;

    @Autowired
    private MoMoAdapter moMoAdapter;

    @Autowired
    private SepayAdapter sepayAdapter;


    @Autowired
    private VNPayCallbackAdapter vnPayCallbackAdapter;

    @Autowired
    private MoMoCallbackAdapter moMoCallbackAdapter;

    // vi phạm open/closed nhưng để trình bày không sử dụng factory pattern
    public PaymentResponse pay(String method, PaymentRequest request) {
        String normalizedMethod = normalizeMethod(method);
        switch (normalizedMethod) {
            case "VNPAY":
                return vnPayAdapter.createPayment(request);
            case "MOMO":
                return moMoAdapter.createPayment(request);
            case "SEPAY": // Để trình bày không sử dụng
                return sepayAdapter.createPayment(request);
            default:
                throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

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
