package com.movie.cinema_booking_backend.service.payment.registry;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.payment.service.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> strategies) {
        this.gateways = new HashMap<>();
        for (PaymentGateway strategy : strategies) {
            String method = normalizeMethod(strategy.method());
            if (gateways.containsKey(method)) {
                throw new IllegalStateException("Chiến lược cổng thanh toán bị trùng lặp cho phương thức: " + method);
            }
            gateways.put(method, strategy);
        }
    }

    public PaymentGateway getByMethod(String methodValue) {
        String method = normalizeMethod(methodValue);
        PaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        return gateway;
    }

    private String normalizeMethod(String methodValue) {
        if (methodValue == null || methodValue.trim().isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        return methodValue.trim().toUpperCase();
    }
}
