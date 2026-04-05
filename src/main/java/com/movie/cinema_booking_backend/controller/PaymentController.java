package com.movie.cinema_booking_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.util.UriComponentsBuilder;
import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.PaymentCallbackResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.IAuthService;
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
    private final IAuthService authService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    public PaymentController(
            PaymentProxy paymentService,
            PaymentFacade paymentFacade,
            IAuthService authService) {
        this.paymentService = paymentService;
        this.paymentFacade = paymentFacade;
        this.authService = authService;
    }

    @GetMapping("/user/payment-url")
    public ApiResponse<?> getMethodName(@RequestParam String method,@RequestBody PaymentRequest paymentRequest,Authentication authentication) {
        UserResponse currentUser = authService.getCurrentUser(authentication);
        paymentFacade.rememberPaymentCreator(paymentRequest.getBookingId(), currentUser.getEmail());

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo URL thanh toán thành công")
                .data(paymentService.createPaymentUrl(method, paymentRequest))
                .build();
    }

    @GetMapping("/callback/{method}")
        public ResponseEntity<Void> paymentCallback(
            @PathVariable String method,
            @RequestParam Map<String, String> params) {
        PaymentCallbackResponse callbackResponse = paymentFacade.processPaymentCallback(method, params);

        Map<String, String> data = callbackResponse.getData();

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
            .path("/callpay")
            .queryParam("success", callbackResponse.isSuccess())
            .queryParam("message", callbackResponse.getMessage())
            .queryParam("bookingId", data.getOrDefault("bookingId", ""))
            .queryParam("paymentStatus", data.getOrDefault("paymentStatus", ""))
            .queryParam("method", data.getOrDefault("method", method))
            .queryParam("gatewayCode", data.getOrDefault("gatewayCode", ""))
            .build()
            .encode()
            .toUriString();

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, redirectUrl)
            .build();
    }
    
}
