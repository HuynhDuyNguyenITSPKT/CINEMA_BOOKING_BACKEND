package com.movie.cinema_booking_backend.config;

import jakarta.servlet.http.HttpServletRequest;

public class PaymentConfig {
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }

        String ipAddress;
        try {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getHeader("X-Real-IP");
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                return "127.0.0.1";
            }

            return ipAddress;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
