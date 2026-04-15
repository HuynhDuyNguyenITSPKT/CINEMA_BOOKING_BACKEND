package com.movie.cinema_booking_backend.service.bookingticket.engine.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class CalculationResult {

    private BigDecimal baseSubtotal      = BigDecimal.ZERO;
    private BigDecimal surchargesTotal   = BigDecimal.ZERO;
    private BigDecimal promotionDiscount = BigDecimal.ZERO;
    private BigDecimal taxAmount         = BigDecimal.ZERO;
    private BigDecimal extrasTotal       = BigDecimal.ZERO;

    private final Map<String, BigDecimal> ticketPrices = new LinkedHashMap<>();

    public BigDecimal getFinalTotal() {
        return baseSubtotal
                .subtract(promotionDiscount)
                .add(surchargesTotal)
                .add(taxAmount)
                .add(extrasTotal)
                .max(BigDecimal.ZERO);
    }

    public BigDecimal getBaseSubtotal()      { return baseSubtotal; }
    public void setBaseSubtotal(BigDecimal v) { this.baseSubtotal = v; }

    public BigDecimal getSurchargesTotal()      { return surchargesTotal; }
    public void setSurchargesTotal(BigDecimal v) { this.surchargesTotal = v; }

    public BigDecimal getPromotionDiscount()      { return promotionDiscount; }
    public void setPromotionDiscount(BigDecimal v) { this.promotionDiscount = v; }

    public BigDecimal getTaxAmount()      { return taxAmount; }
    public void setTaxAmount(BigDecimal v) { this.taxAmount = v; }

    public BigDecimal getExtrasTotal()      { return extrasTotal; }
    public void setExtrasTotal(BigDecimal v) { this.extrasTotal = v; }

    public Map<String, BigDecimal> getTicketPrices() { return ticketPrices; }
    public void putTicketPrice(String seatId, BigDecimal price) {
        ticketPrices.put(seatId, price);
    }
}
