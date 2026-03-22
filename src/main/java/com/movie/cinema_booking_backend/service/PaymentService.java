package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.payment.impl.MoMoAdapter;
import com.movie.cinema_booking_backend.service.payment.impl.MoMoCallbackAdapter;
import com.movie.cinema_booking_backend.service.payment.impl.SepayAdapter;
import com.movie.cinema_booking_backend.service.payment.impl.VNPayAdapter;
import com.movie.cinema_booking_backend.service.payment.impl.VNPayCallbackAdapter;
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


    public PaymentResponse pay(String method, PaymentRequest request) {
        String normalizedMethod = normalizeMethod(method);
        switch (normalizedMethod) {
            case "VNPAY":
                return vnPayAdapter.createPayment(request);
            case "MOMO":
                return moMoAdapter.createPayment(request);
            case "SEPAY":
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
