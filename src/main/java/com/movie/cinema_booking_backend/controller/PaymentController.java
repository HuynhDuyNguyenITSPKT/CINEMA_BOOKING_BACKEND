package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.IPaymentSevice;
import com.movie.cinema_booking_backend.service.payment.proxy.PaymentProxy;

import org.springframework.web.bind.annotation.*;

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
                .message("Payment created successfully")
                .data(paymentService.pay(method, request))
                .build();
    }

    @GetMapping("/callback/vnpay")
    public ApiResponse<PaymentResult> vnpayCallback(@RequestParam Map<String, String> params) {
        return new ApiResponse.Builder<PaymentResult>()
                .success(true)
                .message("VNPAY callback processed successfully")
                .data(paymentService.handleCallback("VNPAY", params))
                .build();
    }

    @GetMapping("/callback/momo")
    public ApiResponse<PaymentResult> momoCallback(@RequestBody Map<String, String> body) {
        return new ApiResponse.Builder<PaymentResult>()
                .success(true)
                .message("MOMO callback processed successfully")
                .data(paymentService.handleCallback("MOMO", body))
                .build();
    }
}
