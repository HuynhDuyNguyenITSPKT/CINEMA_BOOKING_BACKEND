package com.movie.cinema_booking_backend.service;

import java.time.LocalDate;
import java.util.Map;

public interface IDashboardService {

    Map<String, Object> getKeyMetrics(LocalDate fromDate, LocalDate toDate);

    Map<String, Object> getRevenueTrend(LocalDate fromDate, LocalDate toDate, String granularity);

    Map<String, Object> getRevenueBreakdown(LocalDate fromDate, LocalDate toDate);

    Map<String, Object> getShowtimeHeatmap(LocalDate fromDate, LocalDate toDate);

    Map<String, Object> getTopMovies(LocalDate fromDate, LocalDate toDate, int limit, String metric);

    Map<String, Object> getTopExtraServices(LocalDate fromDate, LocalDate toDate, int limit);

    Map<String, Object> getAuditoriumPerformance(LocalDate fromDate, LocalDate toDate);

    Map<String, Object> getNextWeekRevenueForecast();

    Map<String, Object> getCapacityAlerts(double threshold, int hoursAhead);

    Map<String, Object> getExtraServiceSpikeAlerts(double multiplier, int lookbackDays);

    Map<String, Object> getExcelReportData(LocalDate fromDate, LocalDate toDate);

    String exportTransactionsAsCsv(LocalDate fromDate, LocalDate toDate);

    Map<String, Object> getMonthlyPdfSummary(int year, int month);

    Map<String, Object> getLiveSales(int minutes);

    Map<String, Object> getSystemStatus();
}
