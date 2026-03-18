package com.movie.cinema_booking_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TicketPromotionId implements Serializable {
    @Column(name = "ticket_id")
    private String ticketId;

    @Column(name = "promotion_id")
    private String promotionId;
}