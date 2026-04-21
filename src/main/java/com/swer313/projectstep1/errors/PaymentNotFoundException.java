package com.swer313.projectstep1.errors;



public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }
}