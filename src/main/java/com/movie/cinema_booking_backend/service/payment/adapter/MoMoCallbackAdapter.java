package com.movie.cinema_booking_backend.service.payment.adapter;

import com.movie.cinema_booking_backend.service.payment.MoMoService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MoMoCallbackAdapter extends AbstractPaymentCallbackTemplate {

    private final MoMoService moMoService;

    public MoMoCallbackAdapter(MoMoService moMoService) {
        this.moMoService = moMoService;
    }

    @Override
    protected boolean verifySignature(Map<String, String> data) {
        return moMoService.verify(data);
    }

    @Override
    protected boolean isSuccess(Map<String, String> data) {
        return "0".equals(data.get("resultCode"));
    }

    @Override
    protected String provider() {
        return "MOMO";
    }

    @Override
    protected String extractOrderId(Map<String, String> data) {
        return data.get("orderId");
    }

    @Override
    protected String extractResultCode(Map<String, String> data) {
        return data.getOrDefault("resultCode", "");
    }

    @Override
    protected String extractMessage(Map<String, String> data) {
        return data.getOrDefault("message", "");
    }
}
