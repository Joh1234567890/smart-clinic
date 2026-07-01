package com.smartclinic.smartclinic.service;

import com.smartclinic.smartclinic.dto.CreateMedicalRecordRequest;
import com.smartclinic.smartclinic.entity.Appointment;
import com.smartclinic.smartclinic.entity.AppointmentStatus;
import com.smartclinic.smartclinic.entity.Doctor;
import com.smartclinic.smartclinic.entity.MedicalRecord;
import com.smartclinic.smartclinic.entity.Role;
import com.smartclinic.smartclinic.exception.ForbiddenOperationException;
import com.smartclinic.smartclinic.exception.IllegalStateConflictException;
import com.smartclinic.smartclinic.exception.ResourceNotFoundException;
import com.smartclinic.smartclinic.repository.AppointmentRepository;
import com.smartclinic.smartclinic.repository.MedicalRecordRepository;
import com.smartclinic.smartclinic.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Medical record business logic.
 *
 * Ownership rules (enforced here, not just via @PreAuthorize role checks):
 *   - Only a DOCTOR can create a record, and only for an appointment
 *     that is assigned to them.
 *   - A PATIENT may only read their own records.
 *   - A DOCTOR may look up records for a given patient (e.g. reviewing
 *     history before a new appointment), but only ones tied to one of
 *     their own appointments.
 *   - An ADMIN may read everything.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserResolver currentUserResolver;

    // ------------------------------------------------------------------
    // Phase 3: creation & ownership-aware retrieval
    // ------------------------------------------------------------------

    /**
     * POST /api/medical-records/create - DOCTOR only.
     * The record is tied to a specific appointment; patient and doctor
     * are derived from that appointment (never trusted from the
     * request), and the appointment must already be COMPLETED and
     * assigned to the calling doctor.
     */
    public MedicalRecord createMedicalRecord(CreateMedicalRecordRequest request) {
        Doctor caller = currentUserResolver.getCurrentDoctor();

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", request.getAppointmentId()));

        if (!appointment.getDoctor().getId().equals(caller.getId())) {
            throw new ForbiddenOperationException("You can only create records for your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateConflictException(
                    "Cannot create a medical record until the appointment is COMPLETED (current status: "
                            + appointment.getStatus() + ")");
        }

        if (medicalRecordRepository.findByAppointment_Id(appointment.getId()).isPresent()) {
            throw new IllegalStateConflictException("A medical record already exists for this appointment");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setPatient(appointment.getPatient());
        record.setDoctor(appointment.getDoctor());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setNotes(request.getNotes());

        return medicalRecordRepository.save(record);
    }

    /**
     * GET /api/medical-records/my - the current PATIENT's own records.
     */
    @Transactional(readOnly = true)
    public List<MedicalRecord> getMyMedicalRecords() {
        var patient = currentUserResolver.getCurrentPatient();
        return medicalRecordRepository.findByPatientId(patient.getId());
    }

    /**
     * GET /api/medical-records/patient/{id} - DOCTOR or ADMIN.
     * A doctor only sees the subset of that patient's records that came
     * from one of their own appointments; an admin sees all of them.
     */
    @Transactional(readOnly = true)
    public List<MedicalRecord> getRecordsForPatient(Long patientId) {
        var currentUser = currentUserResolver.getCurrentUser();

        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);

        if (currentUser.getRole() == Role.ADMIN) {
            return records;
        }

        // DOCTOR: filter down to only records created from their own appointments.
        Doctor caller = currentUserResolver.getCurrentDoctor();
        return records.stream()
                .filter(record -> record.getDoctor().getId().equals(caller.getId()))
                .toList();
    }

    // ------------------------------------------------------------------
    // Plain CRUD retained from Phase 1 (generic admin-style management)
    // ------------------------------------------------------------------

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", id));
    }

    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord updatedRecord) {
        MedicalRecord existing = getMedicalRecordById(id);
        existing.setDiagnosis(updatedRecord.getDiagnosis());
        existing.setTreatment(updatedRecord.getTreatment());
        existing.setNotes(updatedRecord.getNotes());
        existing.setAppointment(updatedRecord.getAppointment());
        existing.setPatient(updatedRecord.getPatient());
        existing.setDoctor(updatedRecord.getDoctor());
        return medicalRecordRepository.save(existing);
    }

    public void deleteMedicalRecord(Long id) {
        MedicalRecord existing = getMedicalRecordById(id);
        medicalRecordRepository.delete(existing);
    }
}
