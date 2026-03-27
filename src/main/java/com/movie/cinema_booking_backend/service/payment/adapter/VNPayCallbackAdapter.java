package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.service.payment.VNPayService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VNPayCallbackAdapter extends AbstractPaymentCallbackTemplate {

    private final VNPayService vnPayService;

    public VNPayCallbackAdapter(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @Override
    protected boolean verifySignature(Map<String, String> data) {
        return vnPayService.verify(data);
    }

    @Override
    protected boolean isSuccess(Map<String, String> data) {
        return "00".equals(data.get("vnp_ResponseCode"));
    }

    @Override
    protected String provider() {
        return "VNPAY";
    }

    @Override
    protected String extractOrderId(Map<String, String> data) {
        return data.get("vnp_TxnRef");
    }

    @Override
    protected String extractResultCode(Map<String, String> data) {
        return data.getOrDefault("vnp_ResponseCode", "");
    }

    @Override
    protected String extractMessage(Map<String, String> data) {
        return data.getOrDefault("vnp_OrderInfo", "");
    }
}