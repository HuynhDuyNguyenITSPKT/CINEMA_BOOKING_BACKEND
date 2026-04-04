package com.movie.cinema_booking_backend.service.movie.strategy;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricingStrategyContext {

    private final List<IPricingStrategy> strategies;

    public PricingStrategyContext(List<IPricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public int getPrice(int standardPrice, LocalDateTime startTime) {
        IPricingStrategy applicableStrategy = strategies.stream()
                .filter(strategy -> strategy.isApplicable(startTime))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy chiến lược giá phù hợp cho thời gian: " + startTime));

        return applicableStrategy.calculatePrice(standardPrice, startTime);
    }
}
