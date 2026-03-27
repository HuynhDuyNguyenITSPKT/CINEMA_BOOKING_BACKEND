package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.payment.service.PaymentCallbackAdapter;

import java.util.Map;

public abstract class AbstractPaymentCallbackTemplate implements PaymentCallbackAdapter {

    @Override
    public final String method() {
        return provider();
    }

    @Override
    public final PaymentResult handleCallback(Map<String, String> data) {
        // Template Method: khung xử lý cố định cho mọi cổng thanh toán.
        validateInput(data);

        if (!verifySignature(data)) {
            throw new AppException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        String provider = provider();

        return PaymentResult.builder()
                .orderId(extractOrderId(data))
                .success(isSuccess(data))
                .provider(provider)
                .resultCode(extractResultCode(data))
                .message(extractMessage(data))
                .build();
    }

    protected void validateInput(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
    }

    // Hook methods: mỗi provider tự định nghĩa phần khác nhau.
    protected abstract boolean verifySignature(Map<String, String> data);

    protected abstract boolean isSuccess(Map<String, String> data);

    protected abstract String provider();

    protected abstract String extractOrderId(Map<String, String> data);

    protected abstract String extractResultCode(Map<String, String> data);

    protected abstract String extractMessage(Map<String, String> data);
}