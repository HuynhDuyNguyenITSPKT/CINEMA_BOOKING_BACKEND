package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/key-metrics")
    public ApiResponse<?> getKeyMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.builder()
                .success(true)
                .message("Dashboard key metrics fetched successfully")
                .data(dashboardService.getKeyMetrics(fromDate, toDate))
                .build();
    }

    @GetMapping("/trends/revenue")
    public ApiResponse<?> getRevenueTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "DAY") String granularity) {
        return ApiResponse.builder()
                .success(true)
                .message("Revenue trend fetched successfully")
                .data(dashboardService.getRevenueTrend(fromDate, toDate, granularity))
                .build();
    }

    @GetMapping("/trends/revenue-breakdown")
    public ApiResponse<?> getRevenueBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.builder()
                .success(true)
                .message("Revenue breakdown fetched successfully")
                .data(dashboardService.getRevenueBreakdown(fromDate, toDate))
                .build();
    }

    @GetMapping("/trends/showtime-heatmap")
    public ApiResponse<?> getShowtimeHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.builder()
                .success(true)
                .message("Showtime heatmap fetched successfully")
                .data(dashboardService.getShowtimeHeatmap(fromDate, toDate))
                .build();
    }

    @GetMapping("/performance/top-movies")
    public ApiResponse<?> getTopMovies(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "REVENUE") String metric) {
        return ApiResponse.builder()
                .success(true)
                .message("Top movies fetched successfully")
                .data(dashboardService.getTopMovies(fromDate, toDate, limit, metric))
                .build();
    }

    @GetMapping("/performance/top-extra-services")
    public ApiResponse<?> getTopExtraServices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.builder()
                .success(true)
                .message("Top extra services fetched successfully")
                .data(dashboardService.getTopExtraServices(fromDate, toDate, limit))
                .build();
    }

    @GetMapping("/performance/auditoriums")
    public ApiResponse<?> getAuditoriumPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.builder()
                .success(true)
                .message("Auditorium performance fetched successfully")
                .data(dashboardService.getAuditoriumPerformance(fromDate, toDate))
                .build();
    }

    @GetMapping("/forecast/next-week-revenue")
    public ApiResponse<?> getNextWeekRevenueForecast() {
        return ApiResponse.builder()
                .success(true)
                .message("Next week revenue forecast fetched successfully")
                .data(dashboardService.getNextWeekRevenueForecast())
                .build();
    }

    @GetMapping("/forecast/capacity-alerts")
    public ApiResponse<?> getCapacityAlerts(
            @RequestParam(defaultValue = "0.85") double threshold,
            @RequestParam(defaultValue = "48") int hoursAhead) {
        return ApiResponse.builder()
                .success(true)
                .message("Capacity alerts fetched successfully")
                .data(dashboardService.getCapacityAlerts(threshold, hoursAhead))
                .build();
    }

    @GetMapping("/forecast/extra-service-spikes")
    public ApiResponse<?> getExtraServiceSpikeAlerts(
            @RequestParam(defaultValue = "2.0") double multiplier,
            @RequestParam(defaultValue = "14") int lookbackDays) {
        return ApiResponse.builder()
                .success(true)
                .message("Extra service spike alerts fetched successfully")
                .data(dashboardService.getExtraServiceSpikeAlerts(multiplier, lookbackDays))
                .build();
    }

    @GetMapping("/reports/excel-data")
    public ApiResponse<?> getExcelReportData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.builder()
                .success(true)
                .message("Excel report data fetched successfully")
                .data(dashboardService.getExcelReportData(fromDate, toDate))
                .build();
    }

    @GetMapping(value = "/reports/export-excel.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportTransactionsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        String csv = dashboardService.exportTransactionsAsCsv(fromDate, toDate);
        byte[] data = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-transactions.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(data);
    }

    @GetMapping("/reports/pdf-summary")
    public ApiResponse<?> getMonthlyPdfSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.builder()
                .success(true)
                .message("Monthly PDF summary data fetched successfully")
                .data(dashboardService.getMonthlyPdfSummary(year, month))
                .build();
    }

    @GetMapping("/realtime/live-sales")
    public ApiResponse<?> getLiveSales(@RequestParam(defaultValue = "30") int minutes) {
        return ApiResponse.builder()
                .success(true)
                .message("Live sales fetched successfully")
                .data(dashboardService.getLiveSales(minutes))
                .build();
    }

    @GetMapping("/realtime/system-status")
    public ApiResponse<?> getSystemStatus() {
        return ApiResponse.builder()
                .success(true)
                .message("System status fetched successfully")
                .data(dashboardService.getSystemStatus())
                .build();
    }
}
