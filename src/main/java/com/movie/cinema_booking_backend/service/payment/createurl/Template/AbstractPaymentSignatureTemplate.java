package com.movie.cinema_booking_backend.service.payment.createurl.Template;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class AbstractPaymentSignatureTemplate {

    // Bước ký dùng chung: build raw data theo gateway rồi tạo HMAC.
    protected final String signRequest(Map<String, String> params) {
        String rawData = buildRawDataForSigning(params);
        return hmac(rawData);
    }

    // Bước verify callback dùng chung: so sánh chữ ký nhận được với chữ ký tính lại.
    protected final boolean verifyCallback(Map<String, String> params) {
        String receivedSignature = extractReceivedSignature(params);
        if (receivedSignature == null || receivedSignature.trim().isEmpty()) {
            return false;
        }

        String rawData = buildRawDataForVerification(params);
        String calculatedSignature = hmac(rawData);
        return calculatedSignature.equalsIgnoreCase(receivedSignature);
    }

    protected abstract String buildRawDataForSigning(Map<String, String> params);

    protected abstract String buildRawDataForVerification(Map<String, String> params);

    protected abstract String extractReceivedSignature(Map<String, String> params);

    protected abstract String getSecretKey();

    protected abstract String getHmacAlgorithm();

    // Hàm HMAC nội bộ, trả về chuỗi hex lowercase.
    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance(getHmacAlgorithm());
            SecretKeySpec secretKeySpec = new SecretKeySpec(getSecretKey().getBytes(StandardCharsets.UTF_8), getHmacAlgorithm());
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}