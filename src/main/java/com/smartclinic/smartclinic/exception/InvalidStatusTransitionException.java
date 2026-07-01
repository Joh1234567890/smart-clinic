package com.smartclinic.smartclinic.exception;

/**
 * Thrown when a requested appointment status transition violates the
 * enforced lifecycle (PENDING -> APPROVED/REJECTED -> COMPLETED).
 * E.g. trying to complete a PENDING appointment, or approve one that's
 * already COMPLETED. Mapped to HTTP 409 Conflict by
 * GlobalExceptionHandler, since the request is well-formed but
 * conflicts with the resource's current state.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
