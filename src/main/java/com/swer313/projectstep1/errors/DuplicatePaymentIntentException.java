package com.swer313.projectstep1.errors;


import com.swer313.projectstep1.errors.ConflictException;

public class DuplicatePaymentIntentException extends ConflictException {

    public DuplicatePaymentIntentException(Long bookingId) {
        super("A payable payment already exists for booking id: " + bookingId);
    }
}