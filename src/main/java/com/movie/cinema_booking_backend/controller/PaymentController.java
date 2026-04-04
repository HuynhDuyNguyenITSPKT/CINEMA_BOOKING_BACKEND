package com.movie.cinema_booking_backend.controller;

import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.PaymentCallbackResponse;
import com.movie.cinema_booking_backend.service.IPayment;
import com.movie.cinema_booking_backend.service.payment.createurl.facade.PaymentFacade;
import com.movie.cinema_booking_backend.service.payment.createurl.proxy.PaymentProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/payment")
@Validated
public class PaymentController {
    private IPayment paymentService;
    private final PaymentFacade paymentFacade;

    public PaymentController(
            PaymentProxy paymentService,
            PaymentFacade paymentFacade) {
        this.paymentService = paymentService;
        this.paymentFacade = paymentFacade;
    }

    @GetMapping("/user/payment-url")
    public ApiResponse<?> getMethodName(@RequestParam String method,@RequestBody PaymentRequest paymentRequest) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo URL thanh toán thành công")
                .data(paymentService.createPaymentUrl(method, paymentRequest))
                .build();
    }

    @GetMapping("/callback/{method}")
    public ApiResponse<?> paymentCallback(
            @PathVariable String method,
            @RequestParam Map<String, String> params) {
        PaymentCallbackResponse callbackResponse = paymentFacade.processPaymentCallback(method, params);

        return new ApiResponse.Builder<>()
                .success(callbackResponse.isSuccess())
                .message(callbackResponse.getMessage())
                .data(callbackResponse.getData())
                .build();
    }
    
}
