package com.smartclinic.smartclinic.exception;

/**
 * Thrown by services when an authenticated, correctly-roled user tries
 * to act on a resource they don't own - e.g. a PATIENT requesting an
 * appointment that belongs to a different patient, or a DOCTOR trying
 * to approve an appointment assigned to a different doctor.
 *
 * Distinct from Spring Security's AccessDeniedException (which fires
 * when the caller's *role* doesn't match what an endpoint requires).
 * This one fires when the role is right but the specific resource isn't
 * theirs. Both are mapped to HTTP 403 by GlobalExceptionHandler.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
