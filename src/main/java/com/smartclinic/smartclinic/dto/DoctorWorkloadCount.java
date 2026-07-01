package com.smartclinic.smartclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * One bar on the "doctor workload" chart: how many appointments a given
 * doctor has handled in total (across all statuses).
 */
@Getter
@Setter
@AllArgsConstructor
public class DoctorWorkloadCount {
    private Long doctorId;
    private String doctorName;
    private long appointmentCount;
}
