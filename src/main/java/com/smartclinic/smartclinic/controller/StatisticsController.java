package com.smartclinic.smartclinic.controller;

import com.smartclinic.smartclinic.dto.DashboardStatisticsResponse;
import com.smartclinic.smartclinic.dto.DoctorWorkloadCount;
import com.smartclinic.smartclinic.dto.MonthlyAppointmentCount;
import com.smartclinic.smartclinic.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only analytics endpoints for the admin dashboard.
 *
 * Base path /api/statistics doesn't fall under the existing /admin/**
 * URL-prefix rule in SecurityConfig, so every method here is explicitly
 * restricted with @PreAuthorize - same pattern already used for
 * UserController.
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * GET /api/statistics/dashboard
     * The seven top-line numbers for the admin dashboard's stat cards.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatisticsResponse> getDashboardStatistics() {
        return ResponseEntity.ok(statisticsService.getDashboardStatistics());
    }

    /**
     * GET /api/statistics/appointments/monthly
     * Data for the "Monthly Appointments" chart.
     */
    @GetMapping("/appointments/monthly")
    public ResponseEntity<List<MonthlyAppointmentCount>> getMonthlyAppointmentCounts() {
        return ResponseEntity.ok(statisticsService.getMonthlyAppointmentCounts());
    }

    /**
     * GET /api/statistics/appointments/by-doctor
     * Data for the "Doctor Workload" chart.
     */
    @GetMapping("/appointments/by-doctor")
    public ResponseEntity<List<DoctorWorkloadCount>> getDoctorWorkloadCounts() {
        return ResponseEntity.ok(statisticsService.getDoctorWorkloadCounts());
    }
}
