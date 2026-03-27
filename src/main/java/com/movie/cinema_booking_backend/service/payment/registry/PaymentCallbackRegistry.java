package com.movie.cinema_booking_backend.service.payment.registry;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.payment.service.PaymentCallbackAdapter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentCallbackRegistry {

    private final Map<String, PaymentCallbackAdapter> callbackAdapters;

    public PaymentCallbackRegistry(List<PaymentCallbackAdapter> strategies) {
        this.callbackAdapters = new HashMap<>();
        for (PaymentCallbackAdapter strategy : strategies) {
            String method = normalizeMethod(strategy.method());
            if (callbackAdapters.containsKey(method)) {
                throw new IllegalStateException("Duplicate payment callback strategy for method: " + method);
            }
            callbackAdapters.put(method, strategy);
        }
    }

    public PaymentCallbackAdapter getByMethod(String methodValue) {
        String method = normalizeMethod(methodValue);
        PaymentCallbackAdapter callbackAdapter = callbackAdapters.get(method);
        if (callbackAdapter == null) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }
        return callbackAdapter;
    }

    private String normalizeMethod(String methodValue) {
        if (methodValue == null || methodValue.trim().isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        return methodValue.trim().toUpperCase();
    }
}
