package com.smartclinic.smartclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Response body for GET /api/statistics/dashboard.
 *
 * Field names match the Phase 5 spec exactly. One naming note: the spec
 * calls the fourth status "cancelledAppointments", but this system's
 * actual AppointmentStatus enum has no CANCELLED value - the equivalent
 * terminal "did not happen" status is REJECTED (set when a doctor/admin
 * declines a PENDING request). rejectedAppointments counts that status;
 * nothing is renamed or faked to manufacture a "cancelled" count that
 * doesn't exist in the data.
 */
@Getter
@Setter
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long pendingAppointments;
    private long approvedAppointments;
    private long completedAppointments;
    private long rejectedAppointments;
}
