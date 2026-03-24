package com.movie.cinema_booking_backend.service.payment.proxy;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.impl.PaymentService;

@Service
public class PaymentProxy implements IPaymentSevice{

    private static final Logger log = LoggerFactory.getLogger(PaymentProxy.class);
    private static final Set<String> SUPPORTED_METHODS = Set.of("VNPAY", "MOMO");

    private final PaymentService paymentService;
    
    public PaymentProxy(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    public PaymentResponse pay(String method, PaymentRequest request) {
        String normalizedMethod = normalizeAndValidateMethod(method);
        validatePaymentRequest(request);

        log.info("Processing payment request: method={}, bookingId={}", normalizedMethod, request.getBookingId());
        return paymentService.pay(normalizedMethod, request);
    }
    
    @Override
    public PaymentResult handleCallback(String method, Map<String, String> data) {
        String normalizedMethod = normalizeAndValidateMethod(method);
        if (data == null || data.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        log.info("Handling payment callback: method={}, params={}", normalizedMethod, data.size());
        return paymentService.handleCallback(normalizedMethod, data);
    }

    private String normalizeAndValidateMethod(String method) {
        if (method == null || method.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String normalizedMethod = method.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_METHODS.contains(normalizedMethod)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return normalizedMethod;
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getBookingId() == null || request.getBookingId().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
    
}
