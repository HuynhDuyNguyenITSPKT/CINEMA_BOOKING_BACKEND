package com.movie.cinema_booking_backend.service.payment;

import com.movie.cinema_booking_backend.request.PaymentRequest;
import com.movie.cinema_booking_backend.service.payment.createurl.Template.AbstractPaymentSignatureTemplate;

import jakarta.servlet.http.HttpServletRequest;

import com.movie.cinema_booking_backend.config.PaymentConfig;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.*;

@Service
public class VNPayService extends AbstractPaymentSignatureTemplate {

    @Value("${payment.vnpay.secret-key}")
    private String secretKey;

    @Value("${payment.vnpay.pay-url}")
    private String baseUrl;

    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${payment.vnpay.return-url}")
    private String returnUrl;

    @Value("${payment.vnpay.version:2.1.0}")
    private String version;

    @Value("${payment.vnpay.command:pay}")
    private String command;

    public String createPaymentUrl(PaymentRequest request) {
        HttpServletRequest currentRequest = null;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            currentRequest = attrs.getRequest();
        }
        return createPaymentUrl(request, currentRequest);
    }

    public String createPaymentUrl(PaymentRequest request,HttpServletRequest httpRequest) {
        validateRequest(request);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(request.getAmount() * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_Locale", "vn");
        params.put("vnp_TxnRef", sanitizeTxnRef(request.getBookingId()));
        params.put("vnp_OrderInfo", sanitizeOrderInfo(request.getDescription()));
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", PaymentConfig.getIpAddress(httpRequest));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(calendar.getTime()));
        calendar.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        params.put("vnp_SecureHash", hash(params));
        return buildUrl(params);
    }

    private void validateRequest(PaymentRequest request) {
        if (request == null || request.getAmount() == null || request.getAmount() <= 0) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        if (request.getBookingId() == null || request.getBookingId().trim().isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
    }

    private String sanitizeTxnRef(String bookingId) {
        String normalized = bookingId.trim().replaceAll("[^A-Za-z0-9]", "");
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        return normalized.length() > 40 ? normalized.substring(0, 40) : normalized;
    }

    private String sanitizeOrderInfo(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Thanh toan ve xem phim";
        }
        String normalized = Normalizer.normalize(description.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            normalized = "Thanh toan ve xem phim";
        }
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    public String buildUrl(Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder query = new StringBuilder();

            for (String field : fieldNames) {
                String value = params.get(field);
                if (value != null && !value.isEmpty()) {
                    query.append(URLEncoder.encode(field, StandardCharsets.UTF_8.name()))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                            .append("&");
                }
            }

            String queryString = query.substring(0, query.length() - 1);

            return baseUrl + "?" + queryString;
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    public String hash(Map<String, String> params) {
        return signRequest(params);
    }

    public boolean verify(Map<String, String> params) {
        return verifyCallback(params);
    }

    @Override
    protected String buildRawDataForSigning(Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (String field : fieldNames) {
                if ("vnp_SecureHash".equals(field) || "vnp_SecureHashType".equals(field)) {
                    continue;
                }
                String value = params.get(field);
                if (value != null && !value.isEmpty()) {
                    hashData.append(field)
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                            .append("&");
                }
            }

            if (hashData.length() == 0) {
                throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
            }

            return hashData.substring(0, hashData.length() - 1);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }

    @Override
    protected String buildRawDataForVerification(Map<String, String> params) {
        return buildRawDataForSigning(params);
    }

    @Override
    protected String extractReceivedSignature(Map<String, String> params) {
        return params.get("vnp_SecureHash");
    }

    @Override
    protected String getSecretKey() {
        return secretKey;
    }

    @Override
    protected String getHmacAlgorithm() {
        return "HmacSHA512";
    }
}