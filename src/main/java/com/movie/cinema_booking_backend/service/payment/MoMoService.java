package com.movie.cinema_booking_backend.service.payment;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MoMoService {

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
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                buildCreateUrl(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String resultCode = String.valueOf(body.getOrDefault("resultCode", ""));
        if (!"0".equals(resultCode)) {
            String message = String.valueOf(body.getOrDefault("message", "MoMo payment create failed"));
            throw new RuntimeException("MoMo error " + resultCode + ": " + message);
        }

        Object payUrl = body.get("payUrl");
        if (payUrl == null || payUrl.toString().isBlank()) {
            throw new RuntimeException("MoMo create response missing payUrl");
        }

        return payUrl.toString();
    }

    public String sign(Map<String, String> params) {
        String rawData = "accessKey=" + accessKey +
                "&amount=" + params.get("amount") +
                "&extraData=" + params.getOrDefault("extraData", "") +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo") +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + params.getOrDefault("requestId", "") +
                "&requestType=" + requestType;

        return hmacSHA256(secretKey, rawData);
    }

    private String buildCreateUrl() {
        return endpoint.endsWith("/create") ? endpoint : endpoint + "/create";
    }

    private String normalizeAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        try {
            long parsed = Long.parseLong(amount);
            if (parsed <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            return String.valueOf(parsed);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        String normalized = orderId.trim().replaceAll("[^A-Za-z0-9_-]", "");
        if (normalized.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
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
        String signature = params.get("signature");
        if (signature == null || signature.isEmpty()) {
            return false;
        }

        String rawData;
        if (params.containsKey("resultCode")) {
            rawData = "accessKey=" + accessKey +
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
        } else {
            rawData = "amount=" + params.get("amount") +
                    "&orderId=" + params.get("orderId") +
                    "&orderInfo=" + params.get("orderInfo");
        }

        String calculated = hmacSHA256(secretKey, rawData);

        return calculated.equalsIgnoreCase(signature);
    }


    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(key.getBytes(), "HmacSHA256");

            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}