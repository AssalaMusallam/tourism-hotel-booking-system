package com.swer313.projectstep1.errors;



public class InvalidPaymentStateException extends ConflictException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}