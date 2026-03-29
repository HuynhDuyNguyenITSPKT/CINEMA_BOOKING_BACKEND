package com.movie.cinema_booking_backend.service.payment.createurl.facade;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackResponse {
    private boolean success;
    private String message;
    private Map<String, String> data;
}
