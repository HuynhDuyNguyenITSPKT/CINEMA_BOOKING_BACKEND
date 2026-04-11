package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.enums.BookingStatus;
import com.movie.cinema_booking_backend.enums.Role;
import com.movie.cinema_booking_backend.enums.TicketStatus;
import com.movie.cinema_booking_backend.repository.AccountRepository;
import com.movie.cinema_booking_backend.repository.AuditoriumRepository;
import com.movie.cinema_booking_backend.repository.BookingExtraRepository;
import com.movie.cinema_booking_backend.repository.BookingRepository;
import com.movie.cinema_booking_backend.repository.PendingRegistrationRepository;
import com.movie.cinema_booking_backend.repository.ShowtimeRepository;
import com.movie.cinema_booking_backend.repository.TicketRepository;
import com.movie.cinema_booking_backend.service.IDashboardService;
import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Booking;
import com.movie.cinema_booking_backend.entity.BookingExtra;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingExtraRepository bookingExtraRepository;
    private final ShowtimeRepository showtimeRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final AccountRepository accountRepository;

    @Override
    public Map<String, Object> getKeyMetrics(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = (fromDate == null) ? LocalDate.now() : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;

        LocalDateTime start = atStart(from);
        LocalDateTime end = atEndExclusive(to);

        BigDecimal revenue = getRevenueBetween(start, end);
        long cancelledBookings = getCancelledBookingsCount(start, end);
        long totalBookings = getTotalBookingsCount(start, end);
        BigDecimal cancellationRate = ratioPercent(cancelledBookings, totalBookings);

        long soldTickets = getSoldTicketsCount(start, end);
        long totalCapacity = getShowtimeCapacityCount(start, end);
        BigDecimal occupancyRate = ratioPercent(soldTickets, totalCapacity);

        long transactingUsers = getTransactingUsersCount(start, end);

        // This project currently has no created_at on user/account. Pending registrations are the closest proxy.
        long newRegisteredUsers = getNewRegistrationsProxy(start, end);

        Map<String, Object> users = new HashMap<>();
        users.put("newRegisteredUsers", newRegisteredUsers);
        users.put("transactingUsers", transactingUsers);
        users.put("registrationMetricSource", "pending_registrations.otp_generated_time");

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("fromDate", from);
        metrics.put("toDate", to);
        metrics.put("revenue", revenue);
        metrics.put("users", users);
        metrics.put("occupancyRate", occupancyRate);
        metrics.put("cancellationRate", cancellationRate);
        metrics.put("soldTickets", soldTickets);
        metrics.put("totalCapacity", totalCapacity);
        metrics.put("cancelledBookings", cancelledBookings);
        metrics.put("totalBookings", totalBookings);

        return metrics;
    }

    @Override
    public Map<String, Object> getRevenueTrend(LocalDate fromDate, LocalDate toDate, String granularity) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;

        String safeGranularity = (granularity == null) ? "DAY" : granularity.trim().toUpperCase(Locale.ROOT);
        if (!safeGranularity.equals("DAY") && !safeGranularity.equals("WEEK") && !safeGranularity.equals("MONTH")) {
            safeGranularity = "DAY";
        }

        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                atStart(from),
                atEndExclusive(to)
        );

        Map<String, BigDecimal> groupedRevenue = new HashMap<>();
        for (Booking booking : successBookings) {
            String periodKey = switch (safeGranularity) {
                case "WEEK" -> toIsoWeekKey(booking.getCreatedAt());
                case "MONTH" -> toMonthKey(booking.getCreatedAt());
                default -> booking.getCreatedAt().toLocalDate().toString();
            };

            groupedRevenue.merge(periodKey, nvl(booking.getTotalAmount()), BigDecimal::add);
        }

        List<Map<String, Object>> points = groupedRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("period", entry.getKey());
                    point.put("revenue", entry.getValue().setScale(2, RoundingMode.HALF_UP));
                    return point;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("granularity", safeGranularity);
        result.put("series", points);
        return result;
    }

    @Override
    public Map<String, Object> getRevenueBreakdown(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;
        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus.SUCCESS,
            atStart(from),
            atEndExclusive(to)
        );

        Set<String> bookingIds = successBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        List<Ticket> tickets = bookingIds.isEmpty()
            ? List.of()
            : ticketRepository.findByBookingIdIn(bookingIds);
        List<BookingExtra> extras = bookingIds.isEmpty()
            ? List.of()
            : bookingExtraRepository.findByBookingIdIn(bookingIds);

        BigDecimal ticketRevenue = tickets.stream()
            .filter(this::isSoldTicket)
            .map(Ticket::getPrice)
            .filter(price -> price != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal extraRevenue = extras.stream()
            .map(BookingExtra::getTotalPrice)
            .filter(price -> price != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = ticketRevenue.add(extraRevenue);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("ticketRevenue", ticketRevenue);
        result.put("extraServiceRevenue", extraRevenue);
        result.put("totalBreakdownRevenue", total);
        result.put("ticketRevenuePercent", percent(ticketRevenue, total));
        result.put("extraServiceRevenuePercent", percent(extraRevenue, total));
        return result;
    }

    @Override
    public Map<String, Object> getShowtimeHeatmap(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;

        List<Showtime> showtimes = showtimeRepository.findByStartTimeGreaterThanEqualAndStartTimeLessThan(
            atStart(from),
            atEndExclusive(to)
        );
        Set<String> showtimeIds = showtimes.stream().map(Showtime::getId).collect(Collectors.toSet());

        List<Ticket> tickets = showtimeIds.isEmpty()
            ? List.of()
            : ticketRepository.findByShowtimeIdIn(showtimeIds);

        Map<String, Long> counter = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (!isSoldTicket(ticket) || !isSuccessBooking(ticket.getBooking())) {
            continue;
            }

            LocalDateTime startTime = ticket.getShowtime().getStartTime();
            int day = startTime.getDayOfWeek().getValue();
            int hour = startTime.getHour();
            String key = day + "_" + hour;
            counter.merge(key, 1L, (left, right) -> Long.valueOf(left + right));
        }

        List<Map<String, Object>> heatmap = counter.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String[] parts = entry.getKey().split("_");
                int isoDay = Integer.parseInt(parts[0]);
                int hour = Integer.parseInt(parts[1]);

                Map<String, Object> point = new HashMap<>();
                point.put("dayOfWeek", DayOfWeek.of(isoDay).name());
                point.put("hour", hour);
                point.put("soldTickets", entry.getValue());
                return point;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("heatmap", heatmap);
        return result;
    }

    @Override
    public Map<String, Object> getTopMovies(LocalDate fromDate, LocalDate toDate, int limit, String metric) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String safeMetric = (metric == null) ? "REVENUE" : metric.trim().toUpperCase(Locale.ROOT);

        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                atStart(from),
                atEndExclusive(to)
        );

        Set<String> bookingIds = successBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        List<Ticket> tickets = bookingIds.isEmpty()
                ? List.of()
                : ticketRepository.findByBookingIdIn(bookingIds);

        class MovieAgg {
            String movieId;
            String title;
            long soldTickets;
            BigDecimal revenue = BigDecimal.ZERO;
        }

        Map<String, MovieAgg> aggregate = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (!isSoldTicket(ticket)) {
                continue;
            }

            String movieId = ticket.getShowtime().getMovie().getId();
            MovieAgg agg = aggregate.computeIfAbsent(movieId, key -> {
                MovieAgg created = new MovieAgg();
                created.movieId = key;
                created.title = ticket.getShowtime().getMovie().getTitle();
                return created;
            });
            agg.soldTickets += 1;
            agg.revenue = agg.revenue.add(nvl(ticket.getPrice()));
        }

        Comparator<MovieAgg> comparator = safeMetric.equals("TICKETS")
                ? Comparator.comparingLong((MovieAgg a) -> a.soldTickets).reversed()
                : Comparator.comparing((MovieAgg a) -> a.revenue).reversed();

        List<Map<String, Object>> items = aggregate.values().stream()
                .sorted(comparator)
                .limit(safeLimit)
                .map(agg -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("movieId", agg.movieId);
                    item.put("title", agg.title);
                    item.put("soldTickets", agg.soldTickets);
                    item.put("revenue", agg.revenue.setScale(2, RoundingMode.HALF_UP));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("metric", safeMetric);
        result.put("topMovies", items);
        return result;
    }

    @Override
    public Map<String, Object> getTopExtraServices(LocalDate fromDate, LocalDate toDate, int limit) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;
        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                atStart(from),
                atEndExclusive(to)
        );
        Set<String> bookingIds = successBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        List<BookingExtra> extras = bookingIds.isEmpty()
                ? List.of()
                : bookingExtraRepository.findByBookingIdIn(bookingIds);

        class ExtraAgg {
            Long extraServiceId;
            String name;
            long soldQuantity;
            BigDecimal revenue = BigDecimal.ZERO;
        }

        Map<Long, ExtraAgg> aggregate = new HashMap<>();
        for (BookingExtra extra : extras) {
            Long extraId = extra.getExtraService().getId();
            ExtraAgg agg = aggregate.computeIfAbsent(extraId, key -> {
                ExtraAgg created = new ExtraAgg();
                created.extraServiceId = key;
                created.name = extra.getExtraService().getName();
                return created;
            });
            agg.soldQuantity += extra.getQuantity() == null ? 0 : extra.getQuantity();
            agg.revenue = agg.revenue.add(nvl(extra.getTotalPrice()));
        }

        List<Map<String, Object>> items = aggregate.values().stream()
                .sorted(Comparator.comparingLong((ExtraAgg a) -> a.soldQuantity).reversed())
                .limit(safeLimit)
                .map(agg -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("extraServiceId", agg.extraServiceId);
                    item.put("name", agg.name);
                    item.put("soldQuantity", agg.soldQuantity);
                    item.put("revenue", agg.revenue.setScale(2, RoundingMode.HALF_UP));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("topExtraServices", items);
        return result;
    }

    @Override
    public Map<String, Object> getAuditoriumPerformance(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;

        List<Auditorium> auditoriums = auditoriumRepository.findAll();
        List<Showtime> showtimes = showtimeRepository.findByStartTimeGreaterThanEqualAndStartTimeLessThan(
                atStart(from),
                atEndExclusive(to)
        );

        class AuditoriumAgg {
            String auditoriumId;
            String name;
            long showtimeCount;
            long totalCapacity;
            long soldTickets;
        }

        Map<String, AuditoriumAgg> aggregate = new HashMap<>();
        for (Auditorium auditorium : auditoriums) {
            AuditoriumAgg agg = new AuditoriumAgg();
            agg.auditoriumId = auditorium.getId();
            agg.name = auditorium.getName();
            aggregate.put(auditorium.getId(), agg);
        }

        Map<String, String> showtimeToAuditorium = new HashMap<>();
        for (Showtime showtime : showtimes) {
            String auditoriumId = showtime.getAuditorium().getId();
            showtimeToAuditorium.put(showtime.getId(), auditoriumId);

            AuditoriumAgg agg = aggregate.get(auditoriumId);
            if (agg != null) {
                agg.showtimeCount += 1;
                agg.totalCapacity += showtime.getAuditorium().getSeatCount();
            }
        }

        Set<String> showtimeIds = showtimes.stream().map(Showtime::getId).collect(Collectors.toSet());
        List<Ticket> tickets = showtimeIds.isEmpty() ? List.of() : ticketRepository.findByShowtimeIdIn(showtimeIds);
        for (Ticket ticket : tickets) {
            if (!isSoldTicket(ticket) || !isSuccessBooking(ticket.getBooking())) {
                continue;
            }
            String auditoriumId = showtimeToAuditorium.get(ticket.getShowtime().getId());
            if (auditoriumId != null && aggregate.containsKey(auditoriumId)) {
                aggregate.get(auditoriumId).soldTickets += 1;
            }
        }

        List<Map<String, Object>> items = aggregate.values().stream()
                .sorted(Comparator.comparingLong((AuditoriumAgg a) -> a.soldTickets).reversed())
                .map(agg -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("auditoriumId", agg.auditoriumId);
                    item.put("name", agg.name);
                    item.put("showtimeCount", agg.showtimeCount);
                    item.put("totalCapacity", agg.totalCapacity);
                    item.put("soldTickets", agg.soldTickets);
                    item.put("occupancyRate", ratioPercent(agg.soldTickets, agg.totalCapacity));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("auditoriums", items);
        return result;
    }

    @Override
    public Map<String, Object> getNextWeekRevenueForecast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);

        List<BookingStatus> candidateStatuses = List.of(
            BookingStatus.RESERVED,
            BookingStatus.SUCCESS,
            BookingStatus.PENDING,
            BookingStatus.PENDING_APPROVAL
        );

        List<Booking> candidateBookings = bookingRepository.findByStatusIn(candidateStatuses);
        Set<String> bookingIds = candidateBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        List<Ticket> tickets = bookingIds.isEmpty() ? List.of() : ticketRepository.findByBookingIdIn(bookingIds);

        Map<String, List<Ticket>> ticketsByBooking = tickets.stream()
            .collect(Collectors.groupingBy(t -> t.getBooking().getId()));

        BigDecimal confirmedPrebookRevenue = candidateBookings.stream()
            .filter(booking -> {
                List<Ticket> bookingTickets = ticketsByBooking.getOrDefault(booking.getId(), List.of());
                return bookingTickets.stream().anyMatch(ticket -> {
                LocalDateTime startTime = ticket.getShowtime().getStartTime();
                return !startTime.isBefore(now) && startTime.isBefore(nextWeek);
                });
            })
            .map(Booking::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        List<Booking> historicalBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus.SUCCESS,
            now.minusWeeks(8),
            now
        );

        Map<String, BigDecimal> weeklyRevenue = new HashMap<>();
        for (Booking booking : historicalBookings) {
            weeklyRevenue.merge(toIsoWeekKey(booking.getCreatedAt()), nvl(booking.getTotalAmount()), BigDecimal::add);
        }

        BigDecimal historicalWeeklyAverage;
        if (weeklyRevenue.isEmpty()) {
            historicalWeeklyAverage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal total = weeklyRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            historicalWeeklyAverage = total
                .divide(BigDecimal.valueOf(weeklyRevenue.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal predictedRevenue = historicalWeeklyAverage
                .multiply(new BigDecimal("0.6"))
                .add(confirmedPrebookRevenue.multiply(new BigDecimal("0.4")))
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forecastWindow", "next_7_days");
        result.put("predictedRevenue", predictedRevenue);
        result.put("historicalWeeklyAverage", historicalWeeklyAverage.setScale(2, RoundingMode.HALF_UP));
        result.put("confirmedPrebookRevenue", confirmedPrebookRevenue.setScale(2, RoundingMode.HALF_UP));
        result.put("formula", "predicted = 0.6 * historicalWeeklyAverage + 0.4 * confirmedPrebookRevenue");
        return result;
    }

    @Override
    public Map<String, Object> getCapacityAlerts(double threshold, int hoursAhead) {
        double safeThreshold = Math.max(0.5d, Math.min(threshold, 0.99d));
        int safeHours = Math.max(1, Math.min(hoursAhead, 168));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(safeHours);

        List<Showtime> showtimes = showtimeRepository.findByStartTimeGreaterThanEqualAndStartTimeLessThan(now, end);
        Set<String> showtimeIds = showtimes.stream().map(Showtime::getId).collect(Collectors.toSet());
        List<Ticket> tickets = showtimeIds.isEmpty() ? List.of() : ticketRepository.findByShowtimeIdIn(showtimeIds);

        Map<String, Long> soldByShowtime = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (isSoldTicket(ticket) && isSuccessBooking(ticket.getBooking())) {
                soldByShowtime.merge(
                    ticket.getShowtime().getId(),
                    1L,
                    (left, right) -> Long.valueOf(left + right)
                );
            }
        }

        List<Map<String, Object>> alerts = new ArrayList<>();
        for (Showtime showtime : showtimes) {
            long seatCount = showtime.getAuditorium().getSeatCount();
            long soldTickets = soldByShowtime.getOrDefault(showtime.getId(), 0L);
            BigDecimal occupancy = ratioPercent(soldTickets, seatCount);

            if (occupancy.compareTo(BigDecimal.valueOf(safeThreshold * 100)) >= 0) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("showtimeId", showtime.getId());
                alert.put("startTime", showtime.getStartTime());
                alert.put("movieTitle", showtime.getMovie().getTitle());
                alert.put("auditoriumName", showtime.getAuditorium().getName());
                alert.put("seatCount", seatCount);
                alert.put("soldTickets", soldTickets);
                alert.put("occupancyRate", occupancy);
                alerts.add(alert);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("thresholdPercent", BigDecimal.valueOf(safeThreshold * 100).setScale(2, RoundingMode.HALF_UP));
        result.put("hoursAhead", safeHours);
        result.put("alerts", alerts);
        return result;
    }

    @Override
    public Map<String, Object> getExtraServiceSpikeAlerts(double multiplier, int lookbackDays) {
        double safeMultiplier = Math.max(1.2d, Math.min(multiplier, 5.0d));
        int safeLookbackDays = Math.max(3, Math.min(lookbackDays, 60));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime historyStart = todayStart.minusDays(safeLookbackDays);

        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                historyStart,
                LocalDateTime.now()
        );

        Set<String> bookingIds = successBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        Map<String, LocalDateTime> bookingCreatedAt = successBookings.stream()
                .collect(Collectors.toMap(Booking::getId, Booking::getCreatedAt));

        List<BookingExtra> extras = bookingIds.isEmpty() ? List.of() : bookingExtraRepository.findByBookingIdIn(bookingIds);

        class SpikeAgg {
            Long extraServiceId;
            String name;
            long todayQty;
            long historyQty;
        }

        Map<Long, SpikeAgg> aggregate = new HashMap<>();
        for (BookingExtra extra : extras) {
            String bookingId = extra.getBooking().getId();
            LocalDateTime createdAt = bookingCreatedAt.get(bookingId);
            if (createdAt == null) {
                continue;
            }

            Long extraId = extra.getExtraService().getId();
            SpikeAgg agg = aggregate.computeIfAbsent(extraId, key -> {
                SpikeAgg created = new SpikeAgg();
                created.extraServiceId = key;
                created.name = extra.getExtraService().getName();
                return created;
            });

            long qty = extra.getQuantity() == null ? 0 : extra.getQuantity();
            if (!createdAt.isBefore(todayStart)) {
                agg.todayQty += qty;
            } else {
                agg.historyQty += qty;
            }
        }

        List<Map<String, Object>> alerts = new ArrayList<>();
        for (SpikeAgg agg : aggregate.values()) {
            BigDecimal avgDaily = BigDecimal.valueOf(agg.historyQty)
                    .divide(BigDecimal.valueOf(safeLookbackDays), 2, RoundingMode.HALF_UP);

            BigDecimal thresholdQty = avgDaily.multiply(BigDecimal.valueOf(safeMultiplier));
            if (BigDecimal.valueOf(agg.todayQty).compareTo(thresholdQty) >= 0 && agg.todayQty > 0) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("extraServiceId", agg.extraServiceId);
                alert.put("name", agg.name);
                alert.put("todayQuantity", agg.todayQty);
                alert.put("averageDailyQuantity", avgDaily);
                alert.put("spikeMultiplier", safeMultiplier);
                alerts.add(alert);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lookbackDays", safeLookbackDays);
        result.put("multiplier", safeMultiplier);
        result.put("alerts", alerts);
        return result;
    }

    @Override
    public Map<String, Object> getExcelReportData(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = (fromDate == null) ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = (toDate == null) ? LocalDate.now() : toDate;

        List<Booking> bookings = bookingRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            atStart(from),
            atEndExclusive(to)
        );

        Set<String> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toSet());
        Map<String, List<Ticket>> ticketsByBooking = bookingIds.isEmpty()
            ? Map.of()
            : ticketRepository.findByBookingIdIn(bookingIds).stream()
            .collect(Collectors.groupingBy(ticket -> ticket.getBooking().getId()));
        Map<String, List<BookingExtra>> extrasByBooking = bookingIds.isEmpty()
            ? Map.of()
            : bookingExtraRepository.findByBookingIdIn(bookingIds).stream()
            .collect(Collectors.groupingBy(extra -> extra.getBooking().getId()));

        List<Map<String, Object>> transactions = bookings.stream()
            .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
            .map(booking -> {
                List<Ticket> bookingTickets = ticketsByBooking.getOrDefault(booking.getId(), List.of());
                Set<String> promoCodes = new HashSet<>();
                for (Ticket ticket : bookingTickets) {
                if (ticket.getPromotions() == null) {
                    continue;
                }
                ticket.getPromotions().forEach(tp -> {
                    if (tp.getPromotion() != null && tp.getPromotion().getCode() != null) {
                    promoCodes.add(tp.getPromotion().getCode());
                    }
                });
                }

                BigDecimal extraAmount = extrasByBooking.getOrDefault(booking.getId(), List.of()).stream()
                    .map(BookingExtra::getTotalPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

                Map<String, Object> tx = new LinkedHashMap<>();
                tx.put("bookingId", booking.getId());
                tx.put("createdAt", booking.getCreatedAt());
                tx.put("customerName", booking.getUser() != null ? booking.getUser().getFullName() : "");
                tx.put("customerEmail", booking.getUser() != null ? booking.getUser().getEmail() : "");
                tx.put("totalAmount", nvl(booking.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
                tx.put("status", booking.getStatus());
                tx.put("promotionCode", String.join("|", promoCodes));
                tx.put("extraServiceAmount", extraAmount);
                return tx;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("transactions", transactions);
        result.put("exportHint", "Use /api/admin/dashboard/reports/excel-data to get transaction data for Excel export");
        return result;
    }

    @Override
    public String exportTransactionsAsCsv(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> data = getExcelReportData(fromDate, toDate);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) data.getOrDefault("transactions", List.of());

        StringBuilder csv = new StringBuilder();
        csv.append("Booking ID,Created At,Customer Name,Customer Email,Total Amount,Status,Promotion Code,Extra Service Amount\n");

        for (Map<String, Object> row : rows) {
            csv.append(escapeCsv(row.get("bookingId"))).append(',')
                    .append(escapeCsv(row.get("createdAt"))).append(',')
                    .append(escapeCsv(row.get("customerName"))).append(',')
                    .append(escapeCsv(row.get("customerEmail"))).append(',')
                    .append(escapeCsv(row.get("totalAmount"))).append(',')
                    .append(escapeCsv(row.get("status"))).append(',')
                    .append(escapeCsv(row.get("promotionCode"))).append(',')
                    .append(escapeCsv(row.get("extraServiceAmount")))
                    .append('\n');
        }

        return csv.toString();
    }

    @Override
    public Map<String, Object> getMonthlyPdfSummary(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        Map<String, Object> keyMetrics = getKeyMetrics(from, to);
        Map<String, Object> trend = getRevenueTrend(from, to, "DAY");
        Map<String, Object> breakdown = getRevenueBreakdown(from, to);
        Map<String, Object> topMovies = getTopMovies(from, to, 5, "REVENUE");
        Map<String, Object> topExtras = getTopExtraServices(from, to, 5);
        Map<String, Object> auditoriumPerf = getAuditoriumPerformance(from, to);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportType", "MONTHLY_PDF_SUMMARY_DATA");
        result.put("year", year);
        result.put("month", month);
        result.put("keyMetrics", keyMetrics);
        result.put("revenueTrend", trend);
        result.put("revenueBreakdown", breakdown);
        result.put("topMovies", topMovies.get("topMovies"));
        result.put("topExtraServices", topExtras.get("topExtraServices"));
        result.put("auditoriumPerformance", auditoriumPerf.get("auditoriums"));
        result.put("exportHint", "Frontend can render this payload into visual PDF template");
        return result;
    }

    @Override
    public Map<String, Object> getLiveSales(int minutes) {
        int safeMinutes = Math.max(1, Math.min(minutes, 1440));
        LocalDateTime from = LocalDateTime.now().minusMinutes(safeMinutes);

        List<Booking> bookings = bookingRepository.findTop50ByStatusAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            BookingStatus.SUCCESS,
            from
        );

        List<Map<String, Object>> sales = bookings.stream().map(booking -> {
            Map<String, Object> sale = new LinkedHashMap<>();
            sale.put("bookingId", booking.getId());
            sale.put("createdAt", booking.getCreatedAt());
            sale.put("totalAmount", nvl(booking.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
            sale.put("customerName", booking.getUser() != null ? booking.getUser().getFullName() : "");
            return sale;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("windowMinutes", safeMinutes);
        result.put("liveSales", sales);
        return result;
    }

    @Override
    public Map<String, Object> getSystemStatus() {
        Map<String, Long> auditoriumStatus = auditoriumRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                auditorium -> auditorium.getStatus().name(),
                LinkedHashMap::new,
                Collectors.counting()
            ));

        long adminCount = accountRepository.countByRole(Role.ADMIN);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("auditoriumStatus", auditoriumStatus);
        result.put("adminAccounts", adminCount);
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    private BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                start,
                end
            ).stream()
            .map(Booking::getTotalAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private long getCancelledBookingsCount(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus.CANCELLED,
            start,
            end
        );
    }

    private long getTotalBookingsCount(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
    }

    private long getSoldTicketsCount(LocalDateTime start, LocalDateTime end) {
        List<Booking> successBookings = bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            BookingStatus.SUCCESS,
            start,
            end
        );
        Set<String> bookingIds = successBookings.stream().map(Booking::getId).collect(Collectors.toSet());
        if (bookingIds.isEmpty()) {
            return 0;
        }

        return ticketRepository.findByBookingIdIn(bookingIds).stream()
            .filter(this::isSoldTicket)
            .count();
    }

    private long getShowtimeCapacityCount(LocalDateTime start, LocalDateTime end) {
        return showtimeRepository.findByStartTimeGreaterThanEqualAndStartTimeLessThan(start, end)
            .stream()
            .mapToLong(showtime -> showtime.getAuditorium().getSeatCount())
            .sum();
    }

    private long getTransactingUsersCount(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                BookingStatus.SUCCESS,
                start,
                end
            ).stream()
            .map(Booking::getUser)
            .filter(user -> user != null)
            .map(user -> user.getId())
            .filter(id -> id != null)
            .collect(Collectors.toSet())
            .size();
    }

    private long getNewRegistrationsProxy(LocalDateTime start, LocalDateTime end) {
        return pendingRegistrationRepository.countByOtpGeneratedTimeGreaterThanEqualAndOtpGeneratedTimeLessThan(
            start,
            end
        );
    }

    private BigDecimal ratioPercent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerator.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private boolean isSoldTicket(Ticket ticket) {
        return ticket != null &&
                (ticket.getStatus() == TicketStatus.BOOKED || ticket.getStatus() == TicketStatus.USED);
    }

    private boolean isSuccessBooking(Booking booking) {
        return booking != null && booking.getStatus() == BookingStatus.SUCCESS;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String toIsoWeekKey(LocalDateTime dateTime) {
        WeekFields wf = WeekFields.ISO;
        int week = dateTime.get(wf.weekOfWeekBasedYear());
        int year = dateTime.get(wf.weekBasedYear());
        return String.format("%04d-W%02d", year, week);
    }

    private String toMonthKey(LocalDateTime dateTime) {
        return String.format("%04d-%02d", dateTime.getYear(), dateTime.getMonthValue());
    }

    private LocalDateTime atStart(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime atEndExclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    private String escapeCsv(Object value) {
        String raw = value == null ? "" : String.valueOf(value);
        String escaped = raw.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
