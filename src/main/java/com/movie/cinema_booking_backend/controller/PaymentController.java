package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.payment.proxy.PaymentProxy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final IPaymentSevice paymentService;

    public PaymentController(PaymentProxy paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<PaymentResponse> pay(@RequestParam String method,
                               @RequestBody PaymentRequest request) {
        return new ApiResponse.Builder<PaymentResponse>()
                .success(true)
                .message("Thanh toán thành công")
                .data(paymentService.pay(method, request))
                .build();
    }

    @GetMapping("/callback/{method}")
    public ApiResponse<PaymentResult> callbackGet(@PathVariable String method, @RequestParam Map<String, String> params) {
        return new ApiResponse.Builder<PaymentResult>()
                .success(true)
                .message("Trả về kết quả thành công")
                .data(paymentService.handleCallback(method, params))
                .build();
    }

    @PostMapping("/callback/{method}")
    public ApiResponse<PaymentResult> callbackPost(@PathVariable String method,
                                                    @RequestBody Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            params.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }

        return new ApiResponse.Builder<PaymentResult>()
                .success(true)
                .message("Trả về kết quả thành công")
                .data(paymentService.handleCallback(method, params))
                .build();
    }
}
