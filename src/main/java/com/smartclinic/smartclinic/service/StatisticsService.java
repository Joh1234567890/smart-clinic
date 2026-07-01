package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.DashboardStatisticsResponse;
import com.smartclinic.smartclinic.dto.DoctorWorkloadCount;
import com.smartclinic.smartclinic.dto.MonthlyAppointmentCount;
import com.smartclinic.smartclinic.entity.AppointmentStatus;
import com.smartclinic.smartclinic.repository.AppointmentRepository;
import com.smartclinic.smartclinic.repository.DoctorRepository;
import com.smartclinic.smartclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Read-only aggregation queries backing the admin dashboard's stat cards
 * and charts. Nothing here mutates data - every method is a count or a
 * grouping over existing rows, so the whole service is
 * @Transactional(readOnly = true).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Backs GET /api/statistics/dashboard - the seven top-line numbers
     * for the admin dashboard's stat cards.
     */
    public DashboardStatisticsResponse getDashboardStatistics() {
        return new DashboardStatisticsResponse(
                patientRepository.count(),
                doctorRepository.count(),
                appointmentRepository.count(),
                appointmentRepository.countByStatus(AppointmentStatus.PENDING),
                appointmentRepository.countByStatus(AppointmentStatus.APPROVED),
                appointmentRepository.countByStatus(AppointmentStatus.COMPLETED),
                appointmentRepository.countByStatus(AppointmentStatus.REJECTED)
        );
    }

    /**
     * Backs the "Monthly Appointments" chart: one count per calendar
     * month that has at least one appointment, oldest first.
     */
    public List<MonthlyAppointmentCount> getMonthlyAppointmentCounts() {
        return appointmentRepository.countAppointmentsGroupedByMonth().stream()
                .map(row -> {
                    int year = (int) row[0];
                    int month = (int) row[1];
                    long count = (long) row[2];
                    String label = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;
                    return new MonthlyAppointmentCount(label, count);
                })
                .toList();
    }

    /**
     * Backs the "Doctor Workload" chart: total appointments handled by
     * each doctor (any status), busiest doctor first.
     */
    public List<DoctorWorkloadCount> getDoctorWorkloadCounts() {
        return appointmentRepository.countAppointmentsGroupedByDoctor().stream()
                .map(row -> new DoctorWorkloadCount((Long) row[0], (String) row[1], (long) row[2]))
                .toList();
    }
}
