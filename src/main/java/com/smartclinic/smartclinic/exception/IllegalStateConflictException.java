package com.smartclinic.smartclinic.exception;

/**
 * Thrown when a request is well-formed and the caller is authorized,
 * but the target resource's current state makes the operation invalid
 * right now - e.g. creating a medical record for an appointment that
 * hasn't reached COMPLETED yet, or for one that already has a record.
 *
 * Distinct from InvalidStatusTransitionException, which is specifically
 * about an Appointment's own status field moving somewhere invalid.
 * This one covers state conflicts on other entities/operations. Mapped
 * to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class IllegalStateConflictException extends RuntimeException {

    public IllegalStateConflictException(String message) {
        super(message);
    }
}
