package com.movie.cinema_booking_backend.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.payment.createurl.adapter.IAdapterPay;

@Service
public class PaymentService implements IPayment {

    private final Map<String, IAdapterPay> strategy;

    public PaymentService(List<IAdapterPay> adapters) {
        this.strategy = adapters.stream()
                .collect(Collectors.toMap(
                        a -> a.getType().toLowerCase(),
                        a -> a
                ));
    }

    @Override
    public String createPaymentUrl(String method, PaymentRequest request) {
        IAdapterPay adapter = strategy.get(method.toLowerCase());
        if (adapter == null) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        return adapter.createPaymentUrl(request);
    }   


}
