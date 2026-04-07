package com.movie.cinema_booking_backend.service.payment;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.service.payment.createurl.Template.AbstractPaymentSignatureTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MoMoService extends AbstractPaymentSignatureTemplate {

    private static final Logger log = LoggerFactory.getLogger(MoMoService.class);

    @Value("${payment.momo.partner-code}")
    private String partnerCode;

    @Value("${payment.momo.access-key}")
    private String accessKey;

    @Value("${payment.momo.secret-key}")
    private String secretKey;

    @Value("${payment.momo.endpoint}")
    private String endpoint;

    @Value("${payment.momo.return-url}")
    private String returnUrl;

    @Value("${payment.momo.ipn-url}")
    private String notifyUrl;

    @Value("${payment.momo.request-type:captureWallet}")
    private String requestType;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createPayment(Map<String, String> params) {
        String amount = normalizeAmount(params.get("amount"));
        String orderId = normalizeOrderId(params.get("orderId"));
        String orderInfo = normalizeOrderInfo(params.get("orderInfo"));
        String requestId = UUID.randomUUID().toString();
        String extraData = params.getOrDefault("extraData", "");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("partnerCode", partnerCode);
        requestData.put("accessKey", accessKey);
        requestData.put("requestType", requestType);
        requestData.put("ipnUrl", notifyUrl);
        requestData.put("redirectUrl", returnUrl);
        requestData.put("orderId", orderId);
        requestData.put("orderInfo", orderInfo);
        requestData.put("requestId", requestId);
        requestData.put("extraData", extraData);
        requestData.put("amount", amount);
        requestData.put("lang", "vi");
        requestData.put("autoCapture", true);

        String signature = sign(Map.of(
                "amount", amount,
                "orderId", orderId,
                "orderInfo", orderInfo,
                "requestId", requestId,
                "extraData", extraData
        ));
        requestData.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                buildCreateUrl(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
            );
        } catch (RestClientException ex) {
            log.error("MoMo gateway call failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        String resultCode = String.valueOf(body.getOrDefault("resultCode", ""));
        if (!"0".equals(resultCode)) {
            log.warn("MoMo create payment failed. resultCode={}, message={}",
                    resultCode,
                    String.valueOf(body.getOrDefault("message", "")));
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        Object payUrl = body.get("payUrl");
        if (payUrl == null || payUrl.toString().isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        return payUrl.toString();
    }

    public String sign(Map<String, String> params) {
        return signRequest(params);
    }

    private String buildCreateUrl() {
        return endpoint.endsWith("/create") ? endpoint : endpoint + "/create";
    }

    private String normalizeAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        try {
            long parsed = Long.parseLong(amount);
            if (parsed <= 0) {
                throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
            }
            return String.valueOf(parsed);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
    }

    private String normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        String normalized = orderId.trim().replaceAll("[^A-Za-z0-9_-]", "");
        if (normalized.isBlank()) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    private String normalizeOrderInfo(String orderInfo) {
        if (orderInfo == null || orderInfo.isBlank()) {
            return "Thanh toan ve xem phim";
        }
        String normalized = Normalizer.normalize(orderInfo.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isBlank()) {
            normalized = "Thanh toan ve xem phim";
        }
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    public boolean verify(Map<String, String> params) {
        return verifyCallback(params);
    }

    @Override
    protected String buildRawDataForSigning(Map<String, String> params) {
        return "accessKey=" + accessKey +
                "&amount=" + params.get("amount") +
                "&extraData=" + params.getOrDefault("extraData", "") +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo") +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + params.getOrDefault("requestId", "") +
                "&requestType=" + requestType;
    }

    @Override
    protected String buildRawDataForVerification(Map<String, String> params) {
        if (params.containsKey("resultCode")) {
            return "accessKey=" + accessKey +
                    "&amount=" + params.getOrDefault("amount", "") +
                    "&extraData=" + params.getOrDefault("extraData", "") +
                    "&message=" + params.getOrDefault("message", "") +
                    "&orderId=" + params.getOrDefault("orderId", "") +
                    "&orderInfo=" + params.getOrDefault("orderInfo", "") +
                    "&orderType=" + params.getOrDefault("orderType", "") +
                    "&partnerCode=" + params.getOrDefault("partnerCode", "") +
                    "&payType=" + params.getOrDefault("payType", "") +
                    "&requestId=" + params.getOrDefault("requestId", "") +
                    "&responseTime=" + params.getOrDefault("responseTime", "") +
                    "&resultCode=" + params.getOrDefault("resultCode", "") +
                    "&transId=" + params.getOrDefault("transId", "");
        }

        return "amount=" + params.get("amount") +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo");
    }

    @Override
    protected String extractReceivedSignature(Map<String, String> params) {
        return params.get("signature");
    }

    @Override
    protected String getSecretKey() {
        return secretKey;
    }

    @Override
    protected String getHmacAlgorithm() {
        return "HmacSHA256";
    }


}
