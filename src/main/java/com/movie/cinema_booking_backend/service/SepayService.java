package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.request.payment.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SepayService {

    @Value("${payment.sepay.base-url:https://qr.sepay.vn/img}")
    private String baseUrl;

    @Value("${payment.sepay.bank:Vietcombank}")
    private String bank;

    @Value("${payment.sepay.account-number}")
    private String accountNumber;

    @Value("${payment.sepay.template:compact}")
    private String template;

    @Value("${payment.sepay.default-description:Thanh toan ve xem phim}")
    private String defaultDescription;

    @Value("${payment.sepay.page-url:http://localhost:8080/payment/sepay/checkout.html}")
    private String pageUrl;

    public String createCheckoutPageUrl(PaymentRequest request) {
        long amount = normalizeAmount(request == null ? null : request.getAmount());
        String bookingId = normalizeText(request == null ? null : request.getBookingId(), "");
        String description = normalizeText(request == null ? null : request.getDescription(), defaultDescription);
        String qrImageUrl = buildQrImageUrl(amount, description);

        String separator = pageUrl.contains("?") ? "&" : "?";
        return pageUrl + separator
                + "qr=" + encode(qrImageUrl)
                + "&amount=" + amount
                + "&bookingId=" + encode(bookingId)
                + "&description=" + encode(description)
                + "&bank=" + encode(bank)
                + "&account=" + encode(accountNumber);
    }

    private String buildQrImageUrl(long amount, String description) {
        return baseUrl
                + "?bank=" + encode(bank)
                + "&acc=" + encode(accountNumber)
                + "&template=" + encode(template)
                + "&amount=" + amount
                + "&des=" + encode(description);
    }

    private long normalizeAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return amount;
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
