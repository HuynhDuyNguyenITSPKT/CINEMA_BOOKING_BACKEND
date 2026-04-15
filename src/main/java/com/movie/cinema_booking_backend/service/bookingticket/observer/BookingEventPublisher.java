package com.movie.cinema_booking_backend.service.bookingticket.observer;

import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.Ticket;
import com.movie.cinema_booking_backend.entity.TicketPromotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final BookingPaymentSubject bookingPaymentSubject;
    public void publishSuccess(Booking booking) {
        if (booking == null) return;

        List<String> seatIds = booking.getTickets() != null
                ? booking.getTickets().stream()
                        .map(t -> t.getSeat() != null ? t.getSeat().getId() : null)
                        .filter(Objects::nonNull)
                        .toList()
                : Collections.emptyList();

        List<String> ticketIds = booking.getTickets() != null
                ? booking.getTickets().stream()
                        .map(Ticket::getId)
                        .filter(Objects::nonNull)
                        .toList()
                : Collections.emptyList();

        String showtimeId = booking.getTickets() != null && !booking.getTickets().isEmpty()
                && booking.getTickets().get(0).getShowtime() != null
                ? booking.getTickets().get(0).getShowtime().getId()
                : null;

        String movieName = booking.getTickets() != null && !booking.getTickets().isEmpty()
                && booking.getTickets().get(0).getShowtime() != null
                && booking.getTickets().get(0).getShowtime().getMovie() != null
                ? booking.getTickets().get(0).getShowtime().getMovie().getTitle()
                : null;

        String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;

        String promotionCode = booking.getTickets() == null ? null
                : booking.getTickets().stream()
                        .map(Ticket::getPromotion)
                        .filter(Objects::nonNull)
                        .map(TicketPromotion::getPromotion)
                        .filter(Objects::nonNull)
                        .map(com.movie.cinema_booking_backend.entity.Promotion::getCode)
                        .filter(code -> code != null && !code.isBlank())
                        .findFirst()
                        .orElse(null);

        BookingSuccessEvent event = new BookingSuccessEvent(
                booking.getId(),
                userEmail,
                movieName,
                showtimeId,
                seatIds,
                ticketIds,
                booking.getGrandTotalPrice(),
                promotionCode
        );

        bookingPaymentSubject.setPaymentSuccessState(event);
    }
}
