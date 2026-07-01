package com.smartclinic.smartclinic.controller;

import com.smartclinic.smartclinic.dto.CreateMedicalRecordRequest;
import com.smartclinic.smartclinic.entity.MedicalRecord;
import com.smartclinic.smartclinic.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for MedicalRecord, both the Phase 3 business workflow
 * (create/my/all/patient-lookup) and the Phase 1 generic CRUD retained
 * for direct admin-style management.
 *
 * Base path: /api/medical-records, per the same reasoning as
 * AppointmentController - nested under the existing prefix rather than
 * introducing a second URL namespace for the same resource.
 */
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // ------------------------------------------------------------------
    // Phase 3: workflow
    // ------------------------------------------------------------------

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/create")
    public ResponseEntity<MedicalRecord> createMedicalRecord(@Valid @RequestBody CreateMedicalRecordRequest request) {
        MedicalRecord created = medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/my")
    public ResponseEntity<List<MedicalRecord>> getMyMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getMyMedicalRecords());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecordsForAdmin() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }

    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @GetMapping("/patient/{id}")
    public ResponseEntity<List<MedicalRecord>> getRecordsForPatient(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getRecordsForPatient(id));
    }

    // ------------------------------------------------------------------
    // Phase 1: generic CRUD, retained for ADMIN-style direct management
    // ------------------------------------------------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecord> updateMedicalRecord(@PathVariable Long id, @Valid @RequestBody MedicalRecord medicalRecord) {
        return ResponseEntity.ok(medicalRecordService.updateMedicalRecord(id, medicalRecord));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }
}
