package com.smartclinic.smartclinic.exception;

/**
 * Thrown by services when a requested entity cannot be found by id.
 * Translated to a 404 response by the controller layer / a global
 * exception handler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, Long id) {
        super(entityName + " not found with id: " + id);
    }
}
