package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import com.movie.cinema_booking_backend.request.payment.PaymentResponse;
import com.movie.cinema_booking_backend.request.payment.PaymentResult;
import com.movie.cinema_booking_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public PaymentResponse pay(@RequestParam String method,
                               @RequestBody PaymentRequest request) {
        return paymentService.pay(method, request);
    }

    @GetMapping("/callback/vnpay")
    public PaymentResult vnpayCallback(@RequestParam Map<String, String> params) {
        return paymentService.handleCallback("VNPAY", params);
    }

    @GetMapping("/callback/momo")
    public PaymentResult momoCallback(@RequestBody Map<String, String> body) {
        return paymentService.handleCallback("MOMO", body);
    }
}
