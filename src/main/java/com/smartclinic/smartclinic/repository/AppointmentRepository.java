package com.smartclinic.smartclinic.repository;

import com.smartclinic.smartclinic.entity.Appointment;
import com.smartclinic.smartclinic.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    // Used by StatisticsService - a count query is cheaper than loading
    // every row just to call .size() on the list.
    long countByStatus(AppointmentStatus status);

    /**
     * Number of appointments per calendar month, across all years
     * present in the data. Returns one row per (year, month) pair as
     * Object[]{ year (Integer), month (Integer), count (Long) }, ordered
     * chronologically - exactly the shape a "appointments per month"
     * chart needs.
     */
    @Query("""
            SELECT YEAR(a.date), MONTH(a.date), COUNT(a)
            FROM Appointment a
            GROUP BY YEAR(a.date), MONTH(a.date)
            ORDER BY YEAR(a.date), MONTH(a.date)
            """)
    List<Object[]> countAppointmentsGroupedByMonth();

    /**
     * Number of appointments handled by each doctor. Returns one row per
     * doctor as Object[]{ doctorId (Long), doctorName (String), count (Long) },
     * ordered busiest-first - what a "doctor workload" chart needs.
     * Doctors with zero appointments are intentionally excluded (an INNER
     * join via the implicit "a.doctor" path) since this chart is about
     * comparing relative workload, not auditing who has none.
     */
    @Query("""
            SELECT d.id, u.name, COUNT(a)
            FROM Appointment a
            JOIN a.doctor d
            JOIN d.user u
            GROUP BY d.id, u.name
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> countAppointmentsGroupedByDoctor();
}
